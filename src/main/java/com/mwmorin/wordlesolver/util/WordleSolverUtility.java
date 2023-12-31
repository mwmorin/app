package com.mwmorin.wordlesolver.util;

import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Michael Morin
 * 
 * Determines best next guess in the Wordle game given current guess and result of guess.
 * 
 * 
 * Exposes API that returns next word to guess based on last word guessed and its result. Retrieves saved state given sessionId and persists state after determining best next guess.
 * 	- i) load past results (state) for given sessionId from file into memory
 * 	- ii) determine best next guess
 * 	- iii) store results (state) in file based on sessionId
 * 	- iv) return best next word to guess
 * 	- v) delete all serialized files for given sessionId once game is over (result is 'ggggg' or max guess limit reached)		
 *
 */

// TODO - support max guess limit
// TODO - delete all serialized files for given sessionId once game is over (result is 'ggggg' or max guess limit reached)

public class WordleSolverUtility {

	private static final Logger LOGGER = Logger.getLogger(WordleSolverUtility.class.getName());
	private static final String ENTRY_DELIMITER = ",";
	private static final String KEY_VALUE_DELIMITER = ":";

	private static Properties properties = new Properties();

	//	private static final String englishWordsFilePathName = "C:\\Users\\Michael\\Downloads\\word_list_english.txt";
	//	private static final String fiveLetterWordsAndWeightsFilePathName = "C:\\Users\\Michael\\Downloads\\fiveLetterWordsAndWeights.txt";
	private static final String serializedBaseDir = "/serializedFiles/";
	private static final String englishWordsFilePathName = "/word_list_english.txt";
	private static final String fiveLetterWordsAndWeightsFilePathName = "/fiveLetterWordsAndWeights.txt";

	private String guess = "";
	private String result = "";
	private String serializedSessionDir = "";
	private static List<String> fiveLetterWordsList = new ArrayList<String>();
	private static Map<String, Float> fiveLetterWordsAndWeightsMap = new LinkedHashMap<String, Float>();
	private static HashMap<Character, Float> letterFrequenciesMap = new HashMap<Character, Float>();
	private static HashMap<Character, Float> firstLetterFrequenciesMap = new HashMap<Character, Float>();
	//private static char[] positionsWithRightLetter = {'-','-','-','-','-'};
	private char[] positionsWithRightLetter = new char[5];
	@SuppressWarnings("unchecked")
	private List<Character>[] positionsWithWrongLetters = new ArrayList[5];
	private HashMap<Character, Integer> lettersWithExactOccurrencesKnown = new HashMap<Character, Integer>();
	private HashMap<Character, Integer> lettersWithMinButNotExactOccurrencesKnown = new HashMap<Character, Integer>();
	private static final Character greenChar = 'g';
	private static final Character yellowChar = 'y';
	private static final Character blackChar = 'b';
	private boolean allLettersAreGreenOrYellow = false;


	// Initialize class level vars
	static
	{
		// Read in config values
/*		try (InputStream input = new FileInputStream("/config.properties")) { // NOT FINDING FILE !
			properties.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		}*/

		// TODO - optimze - load properties only when needed (that is, when sorted 5 letter word file does not exist, since current props are used only to build that file)
		try {
			InputStream input = new ClassPathResource("/config.properties").getInputStream();
			properties.load(input);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// Load dictionary sorted by weight
		loadfiveLetterWordsAndWeights();
	}


	public WordleSolverUtility(String sessionId) {
		super();
		
		// Construct serialized session dir
		serializedSessionDir = serializedBaseDir + sessionId + "/";
	}

	
	/**
	 * Returns next word to guess based on last word guessed and its result. 
	 * Persist results (state) in file based on provided sessionId.
	 * 		- i) load past state for given sessionId from file into memory
	 * 		- i) determine next guess
	 * 		- iii) store state in file based on sessionId
	 * 		- iv) return next word to guess
	 * 
	 * Detailed steps:
	 * - Creates file (fiveLetterWordsList.txt) of all five letter English words sorted by weight, if not existing.
	 * - Determine the following rules based on the result of this guess
	 * 		- (rule 1) positions where we know the letter that goes there
	 * 			- these are the positions with green letters
	 * 			- store in char[] positionsWithRightLetter = new char[5]; (example, if we know that 'h' is the 2nd char and 't' is the 5th char: {-,h,-,-,t}); do not wipe out existing chars since to support any guess #
	 * 		- (rule 2) positions where we know what letters do NOT go there (but do not know which letter does go there)
	 * 			- these are positions with black or yellow letters
	 * 			- store in List<Character>[] positionsWithWrongLetters = new ArrayList[5]; (example: { 0:(b,k,c) 1:(h,a,w), ...}); again, add to current list to keep info from previous guesses
	 * 		- (rule 3) letters for which we know exact number of occurrences. 
	 * 			- We know this for the following cases:
	 * 				- letters that have at least one black occurrence
	 * 					- # of occurrences of a black letter = # of yellow + # of green of that letter
	 * 				- all 5 letters are green or yellow (edge case but have seen this happen; in this case, wipe out contents of lettersWithMinButNotExactOccurrencesKnown from rule 4 since all letters are covered in rule 3 now)
	 * 					- # of occurrences of a letter (when all 5 letters are green or yellow) = # of yellow + # of green of that letter
	 * 			- store in HashMap lettersWithExactOccurrencesKnown; example: {d:1 a:2 r:1}; again, add to list since this may not be our first guess
	 * 			- remove letter from lettersWithMinButNotExactOccurrencesKnown (if present) since exact # is known
	 * 		- (rule 4) letters for which we know a non-zero min # of occurrences, but do not know the exact #. 
	 * 			- these are letters that are green or yellow AND never black AND are not in lettersWithExactOccurrencesKnown
	 * 			- min # of occurrences = # of green + # of yellow
	 * 			- store in HashMap lettersWithMinButNotExactOccurrencesKnown; example: {f:1 b:2 c: 2}; if letter already exists in map, then update it's value; if letter does not already exist in map, then add it; leave all other entries intact 
	 * - Determine next word to guess. Steps are:
	 * 		- Begin iterating thru fiveLetterWordsList
	 * 			- check the rules in order; immediately jump to next word if a rule is violated
	 * 			- if all rules pass, then we found our next guess
	 * 
	 *
	 * @param wordGuessed
	 * @param result
	 * @return
	 */
	public String getNextGuess(String wordGuessed, String result)
	{
		this.guess = wordGuessed;
		this.result = result;
		
		String nextWordToGuess = "";

		// Read in serialized data
		deserializeState();

		// Process the results into rules

		/////////////////////////////////////////////////////////////////
		// RULE 1: positions where we know the letter that goes there
		/////////////////////////////////////////////////////////////////

		// 	- these are the positions with green letters
		// 	- store in char[]
		//	- put '-' in a position if letter is not known
		//	- example: if we know that 'h' is the 2nd char and 't' is the 5th char: {-,h,-,-,t}); 
		//	- do not wipe out existing to keep analysis of previous guesses

		for (int resultIterCount = 0; resultIterCount < 5; resultIterCount++) 
		{
			// If result has green, add corresponding guess letter to positionsWithRightLetter
			if (greenChar.equals(result.charAt(resultIterCount)))
			{
				positionsWithRightLetter[resultIterCount] = guess.charAt(resultIterCount);
			}				
		}			
		// DEBUG..
		LOGGER.config("positionsWithRightLetter is: " + new String(positionsWithRightLetter));


		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// RULE 2: positions where we know what letters do NOT go there (but do not know which letter does go there)
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////

		// 	- these are positions with black or yellow letters
		//	- example: { 0:(b,k,c) 1:(h,a,w), ...}
		//	- add to current list to keep info from previous guesses

		for (int resultIterCount = 0; resultIterCount < 5; resultIterCount++) 
		{
			// If result has black or yellow, add corresponding guess letter to positionsWithWrongLetters
			if (blackChar.equals(result.charAt(resultIterCount)) || yellowChar.equals(result.charAt(resultIterCount)))
			{
//				// Instantiate list if none at this index yet
//				if (positionsWithWrongLetters[resultIterCount] == null)
//				{
//					positionsWithWrongLetters[resultIterCount] = new ArrayList<Character>();
//				}
//				
				// Add letters to list
				positionsWithWrongLetters[resultIterCount].add(guess.charAt(resultIterCount));
			}
		}
		// DEBUG..
		for (int i = 0; i < positionsWithWrongLetters.length; i++) 
		{
			LOGGER.config("positionsWithWrongLetters[" + i + "] = " + positionsWithWrongLetters[i]);
		}


		///////////////////////////////////////////////////////////////////
		// RULE 3: letters for which we know exact number of occurrences
		///////////////////////////////////////////////////////////////////

		//	- We know this for the following cases:
		//		- letters that have at least one black occurrence
		//			- # of occurrences of a black letter = # of yellow + # of green of that letter
		//		- all 5 letters are green or yellow (edge case but have seen this happen; in this case, wipe out contents of lettersWithMinButNotExactOccurrencesKnown from rule 4 since all letters covered in rule 3 now)
		//			- # of occurrences of a letter (when all 5 letters are green or yellow) = # of yellow + # of green of that letter
		//	- store in HashMap lettersWithExactOccurrencesKnown (example  d:1 a:2 r:1); again, add to list since this may not be our first guess
		//	- remove letter from lettersWithMinButNotExactOccurrencesKnown (if present) since exact # is known

		// first check the edge case - all 5 letters are green or yellow
		if (allLettersAreGreenOrYellow())
		{
			// All letters are green or yellow. Thus, we know the exact number of each letter. We will therefore wipe out the current contents of lettersWithExactOccurrencesKnown and set it based on current results.
			lettersWithExactOccurrencesKnown.clear();

			for (int resultIterCount = 0; resultIterCount < 5; resultIterCount++) 
			{
				// skip letter if already in lettersWithExactOccurrencesKnown
				if (!lettersWithExactOccurrencesKnown.containsKey(guess.charAt(resultIterCount)))
				{
					int count = countOfYellowAndGreen(guess.charAt(resultIterCount));
					lettersWithExactOccurrencesKnown.put(guess.charAt(resultIterCount), count);
				}
			}				

			// We will also wipe out the contents of lettersWithMinButNotExactOccurrencesKnown from rule 4 since all letters are covered in rule 3 now
			lettersWithMinButNotExactOccurrencesKnown.clear();
		}
		else
			// otherwise, check for letters that have at least one black occurrence
		{
			for (int resultIterCount = 0; resultIterCount < 5; resultIterCount++) 
			{
				// skip letter if already in lettersWithExactOccurrencesKnown
				if (!lettersWithExactOccurrencesKnown.containsKey(guess.charAt(resultIterCount)))
				{
					if (hasBlack(guess.charAt(resultIterCount)))
					{
						int count = countOfYellowAndGreen(guess.charAt(resultIterCount));
						lettersWithExactOccurrencesKnown.put(guess.charAt(resultIterCount), count);

						// remove letter from lettersWithMinButNotExactOccurrencesKnown (if present) since exact # is known
						lettersWithMinButNotExactOccurrencesKnown.remove(guess.charAt(resultIterCount));
					}
				}					
			}				
		}
		// DEBUG..
		LOGGER.config ("After rule 3:");
		LOGGER.config("lettersWithExactOccurrencesKnown = " + lettersWithExactOccurrencesKnown);
		LOGGER.config("lettersWithMinButNotExactOccurrencesKnown = " + lettersWithMinButNotExactOccurrencesKnown);


		/////////////////////////////////////////////////////////////////////////////////////////////////////
		// RULE 4: letters for which we know a non-zero min # of occurrences, but do not know the exact #
		/////////////////////////////////////////////////////////////////////////////////////////////////////			

		//	- these will be letters that are green or yellow AND never black AND are not in lettersWithExactOccurrencesKnown
		//	- skip this rule if we know the exact number of all letters in answer (i.e. when allLettersAreGreenOrYellow is true)
		//	- if letter already exists in map, then update it's value; if letter does not already exist in map, then add it; leave all other entries intact 
		//	- min # of occurrences = # of green + # of yellow

		if (!allLettersAreGreenOrYellow)
		{
			for (int resultIterCount = 0; resultIterCount < 5; resultIterCount++) 
			{
				if (!hasBlack(guess.charAt(resultIterCount)))
				{
					// letter is never black, and, since it's present, has at least one green or yellow
					// skip letter if in lettersWithExactOccurrencesKnown
					if (!lettersWithExactOccurrencesKnown.containsKey(guess.charAt(resultIterCount)))
					{
						int count = countOfYellowAndGreen(guess.charAt(resultIterCount));
						// add or update this letter in map (remove it, then add it)
						lettersWithMinButNotExactOccurrencesKnown.remove(guess.charAt(resultIterCount));
						lettersWithMinButNotExactOccurrencesKnown.put(guess.charAt(resultIterCount), count);							
					}						
				}
			}
		}
		// DEBUG..
		LOGGER.config ("After rule 4:");
		LOGGER.config("lettersWithExactOccurrencesKnown = " + lettersWithExactOccurrencesKnown);
		LOGGER.config("lettersWithMinButNotExactOccurrencesKnown = " + lettersWithMinButNotExactOccurrencesKnown);


		////////////////////////////////////
		// Determine next word to guess
		////////////////////////////////////

		// Determine first word from fiveLetterWordsAndWeightsMap that satisfies all rules

		for (String word : fiveLetterWordsAndWeightsMap.keySet()) 
		{
			// Check word against rules
			if (wordSatisfiesAllRules(word))
			{
				// Word satisfies all rules. Make this the next guess word, and stop looking for next guess.
				nextWordToGuess = word;							
				break;
			}
		}

		// Serialize state
		serializeState();

		return nextWordToGuess;		
	}

	@SuppressWarnings("unused")
	private void deleteAllSerializedFiles()
	{
		// lettersWithExactOccurrencesKnown.serialized.txt
		
		File dir = new File(serializedBaseDir);
		deleteDirectory(dir);
	}
	
	public void deleteAllSerializedSessionFiles()
	{
		// lettersWithExactOccurrencesKnown.serialized.txt
		
		File dir = new File(serializedSessionDir);
		deleteDirectory(dir);
	}
	
	/**
	 * Load saved state from files into memory.
	 * Set to default values if none exist.
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void deserializeState() {
		
		// lettersWithExactOccurrencesKnown
		lettersWithExactOccurrencesKnown = (HashMap<Character, Integer>)deserializeObject("lettersWithExactOccurrencesKnown.serialized.txt");
		if (lettersWithExactOccurrencesKnown == null)
		{
			lettersWithExactOccurrencesKnown = new HashMap<Character, Integer>();
		}
		// DEBUG 
        printMap(lettersWithExactOccurrencesKnown);
        
        // lettersWithMinButNotExactOccurrencesKnown
        lettersWithMinButNotExactOccurrencesKnown = (HashMap<Character, Integer>)deserializeObject("lettersWithMinButNotExactOccurrencesKnown.serialized.txt");
		if (lettersWithMinButNotExactOccurrencesKnown == null)
		{
			lettersWithMinButNotExactOccurrencesKnown = new HashMap<Character, Integer>();
		}
		// DEBUG 
        printMap(lettersWithMinButNotExactOccurrencesKnown);
        
        // positionsWithWrongLetters
        positionsWithWrongLetters = (List<Character>[])deserializeObject("positionsWithWrongLetters.serialized.txt");
        if (positionsWithWrongLetters == null)
        {
        	positionsWithWrongLetters = new ArrayList[5];        	
    		for (int i = 0; i < positionsWithWrongLetters.length; i++) 
    		{
    			positionsWithWrongLetters[i] = new ArrayList<Character>();			
    		}
        }
        
        // positionsWithRightLetter
        // private static char[] positionsWithRightLetter = {'-','-','-','-','-'};
        positionsWithRightLetter = (char[])deserializeObject("positionsWithRightLetter.serialized.txt");
        if (positionsWithRightLetter == null)
        {
        	positionsWithRightLetter = new char[5];
        	for (int i = 0; i < positionsWithRightLetter.length; i++) 
        	{
        		positionsWithRightLetter[i] = '-';
			}
        }


		
	}


	/**
	 * Load deserialized object from given file
	 * @param fileName
	 * @return
	 */
	private Object deserializeObject(String fileName) {
		
		Object deserializedObject = null;
		
            FileInputStream fileInput;
    		try {
    			fileInput = new FileInputStream(serializedSessionDir + fileName);

    			ObjectInputStream objectInput = new ObjectInputStream(fileInput); 
      
    			deserializedObject = objectInput.readObject(); 
      
                objectInput.close(); 
                fileInput.close(); 
    		} catch (FileNotFoundException e) {
    			// Ignore, since file may not exist under normal processing
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (ClassNotFoundException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} 
		
		return deserializedObject;
		
	}


	/**
	 * Persist state to files based on sessionId, so can retrieve state next invocation (next guess) given sessionId
	 */
	// TODO - base on sessionId
	private void serializeState() {
		
		// Create serialized directory for this sessionId if not exists
		File serializedSessionDirFile = new File(serializedSessionDir);
        if (!serializedSessionDirFile.exists()) {
        	File dir = new File(serializedSessionDir);
        	dir.mkdirs();
        }
		
		// Variables to serialize:
//		private static String guess = "";
		// NOT NEEDED (since user enters this each guess)
//		private static String result = "";
		// NOT NEEDED (since user enters this each guess)
//		private static List<String> fiveLetterWordsList = new ArrayList<String>();
		// NOT NEEDED
//		private static Map<String, Float> fiveLetterWordsAndWeightsMap = new LinkedHashMap();
		//loadfiveLetterWordsAndWeights();
     // NOT NEEDED since called from static init
//		private static HashMap<Character, Float> letterFrequenciesMap = new HashMap<Character, Float>();
		// NOT NEEDED		
//		private static HashMap<Character, Float> firstLetterFrequenciesMap = new HashMap<Character, Float>();
		// NOT NEEDED
//		private static char[] positionsWithRightLetter = {'-','-','-','-','-'};
		serializeObject(positionsWithRightLetter, "positionsWithRightLetter.serialized.txt");
//		private static List<Character>[] positionsWithWrongLetters = new ArrayList[5];
		serializeObject(positionsWithWrongLetters, "positionsWithWrongLetters.serialized.txt");
//		private static HashMap<Character, Integer> lettersWithExactOccurrencesKnown = new HashMap<Character, Integer>();
		serializeObject(lettersWithExactOccurrencesKnown, "lettersWithExactOccurrencesKnown.serialized.txt");
		System.out.println("Serialing lettersWithExactOccurrencesKnown..");
		printMap(lettersWithExactOccurrencesKnown);
//		private static HashMap<Character, Integer> lettersWithMinButNotExactOccurrencesKnown = new HashMap<Character, Integer>();
		serializeObject(lettersWithMinButNotExactOccurrencesKnown, "lettersWithMinButNotExactOccurrencesKnown.serialized.txt");
		System.out.println("Serialing lettersWithMinButNotExactOccurrencesKnown..");
		printMap(lettersWithMinButNotExactOccurrencesKnown);
//		private static Character greenChar = 'g';
		// NOT NEEDED (constant)
//		private static Character yellowChar = 'y';
		// NOT NEEDED (constant)
//		private static Character blackChar = 'b';
		// NOT NEEDED (constant)
//		private static boolean allLettersAreGreenOrYellow = false;
		// NOT NEEDED (since call method each guess to set this var)
		 
		 

		
		
	}
	
	/**
	 * Serial object to file with provided name
	 * @param object
	 * @param fileName
	 */
	private void serializeObject(Object object, String fileName)
	{
	    try {
        FileOutputStream myFileOutStream = new FileOutputStream(serializedSessionDir + fileName); 

	    ObjectOutputStream myObjectOutStream = new ObjectOutputStream(myFileOutStream); 
	
	    myObjectOutStream.writeObject(object); 
	
	    // Closing FileOutputStream and ObjectOutputStream 
		myObjectOutStream.close();
		myFileOutStream.close(); 
		
		} catch (IOException e) {
			e.printStackTrace();
		} 
	    
	}


	/**
	 * Read letter frequencies from property file and store in memory
	 * - letterFrequencies
	 * - firstLetterFrequencies
	 */
	private static void readLetterFrequencies() 
	{
		// letterFrequencies
		String letterFrequencies = (String) properties.get("letterFrequencies"); // format letter:frequency,letter:frequency,...
		String[] keyValues = letterFrequencies.split(ENTRY_DELIMITER);
		for (int i = 0; i < keyValues.length; i++) {
			String[] tokens = keyValues[i].split(KEY_VALUE_DELIMITER);
			letterFrequenciesMap.put(tokens[0].charAt(0), Float.valueOf(tokens[1]));
		}
		LOGGER.info("letterFrequenciesMap = " + letterFrequenciesMap);

		// firstLetterFrequencies
		String firstLetterFrequencies = (String) properties.get("firstLetterFrequencies"); // format letter:frequency,letter:frequency,...
		String[] keyValues2 = firstLetterFrequencies.split(ENTRY_DELIMITER);
		for (int i = 0; i < keyValues2.length; i++) {
			String[] tokens = keyValues2[i].split(KEY_VALUE_DELIMITER);
			firstLetterFrequenciesMap.put(tokens[0].charAt(0), Float.valueOf(tokens[1]));
		}
		LOGGER.info("firstLetterFrequenciesMap = " + firstLetterFrequenciesMap);


	}

	/**
	 * Create the 5 letter words and weights from file, if it does not exist.
	 * Loads 5 letter words and weights into memory.
	 * 
	 */
/*	private static void loadfiveLetterWordsAndWeightsBAD() {
		//			- check if fiveLetterWordsAndWeights.txt exists
		//			- if not exists 
		//				- read in word list
		//				- for each 5 letter word:
		//					- determine it's weight (using weighWord())
		//					- store word and weight in file fiveLetterWordsAndWeights.txt (format: word,weight)
		//		- read fiveLetterWordsAndWeights.txt in memory (hashmap (word,weight))

		//File file = new File(fiveLetterWordsAndWeightsFilePathName);
		File file = null;
		try {
			file = new ClassPathResource(fiveLetterWordsAndWeightsFilePathName).getFile(); // TODO - cannot find file when running via jar
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		if (!file.exists()) {
			// file does not exist, build 5 letter word and weight list   	 

			// read in letter frequencies from properties
			readLetterFrequencies();

			// get all 5 letter words from all words list file and load into memory
			loadFiveLetterWords();

			// determine weight of each 5 letter word and store map fiveLetterWordsAndWeightsMap
			for (Iterator<String> iterator = fiveLetterWordsList.iterator(); iterator.hasNext();) 
			{
				String word = iterator.next();

				// determine word weight
				float weight = weighWord(word);

				// store 5 letter words and weights in memory
				fiveLetterWordsAndWeightsMap.put(word, weight);				
			}

			// Sort the fiveLetterWordsAndWeightsMap entries by value (weight) in descending order
			fiveLetterWordsAndWeightsMap = fiveLetterWordsAndWeightsMap.entrySet()
					.stream()
					.sorted(Collections.reverseOrder(Entry.comparingByValue()))
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

			// persist 5 letter words and weights to file for later program executions
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(fiveLetterWordsAndWeightsFilePathName));

				for (String word : fiveLetterWordsAndWeightsMap.keySet()) 
				{
					writer.write(word + ENTRY_DELIMITER + fiveLetterWordsAndWeightsMap.get(word));
					writer.newLine();
				}

				writer.close();

			} catch (IOException e1) {
				e1.printStackTrace();
			} 			
		} 	
		else
		{
			// file already exists, load contents into memory
			try {
				// Open the file of all English words
				//FileInputStream fstream = new FileInputStream(fiveLetterWordsAndWeightsFilePathName);
				InputStream istream = new ClassPathResource(fiveLetterWordsAndWeightsFilePathName).getInputStream();


				DataInputStream in = new DataInputStream(istream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));

				String strLine;

				// Read File Line By Line and store in fiveLetterWordsAndWeights
				while ((strLine = br.readLine()) != null) {
					// split into word and weight tokens
					String[] tokens = strLine.split(ENTRY_DELIMITER); // 0 = word, 1 = weight
					fiveLetterWordsAndWeightsMap.put(tokens[0], Float.valueOf(tokens[1]));
				}

				// Close the input stream
				in.close();
			} catch (Exception e) { // Catch exception if any
				e.printStackTrace();
			}

		}
	}*/

	private static void loadfiveLetterWordsAndWeights() {
		//			- check if fiveLetterWordsAndWeights.txt exists
		//			- if not exists
		//				- read in word list
		//				- for each 5 letter word:
		//					- determine it's weight (using weighWord())
		//					- store word and weight in file fiveLetterWordsAndWeights.txt (format: word,weight)
		//		- read fiveLetterWordsAndWeights.txt in memory (hashmap (word,weight))

		boolean fileAlreadyExists = true;

		InputStream istream = null;
		try {
			istream = new ClassPathResource(fiveLetterWordsAndWeightsFilePathName).getInputStream();
		} catch (FileNotFoundException e) {
			fileAlreadyExists = false;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}


		if (!fileAlreadyExists) {
			// file does not exist, build 5 letter word and weight list

			// read in letter frequencies from properties
			readLetterFrequencies();

			// get all 5 letter words from all words list file and load into memory
			loadFiveLetterWords();

			// determine weight of each 5 letter word and store map fiveLetterWordsAndWeightsMap
			for (Iterator<String> iterator = fiveLetterWordsList.iterator(); iterator.hasNext();)
			{
				String word = iterator.next();

				// determine word weight
				float weight = weighWord(word);

				// store 5 letter words and weights in memory
				fiveLetterWordsAndWeightsMap.put(word, weight);
			}

			// Sort the fiveLetterWordsAndWeightsMap entries by value (weight) in descending order
			fiveLetterWordsAndWeightsMap = fiveLetterWordsAndWeightsMap.entrySet()
					.stream()
					.sorted(Collections.reverseOrder(Entry.comparingByValue()))
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

			// persist 5 letter words and weights to file for later program executions
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(fiveLetterWordsAndWeightsFilePathName));

				for (String word : fiveLetterWordsAndWeightsMap.keySet())
				{
					writer.write(word + ENTRY_DELIMITER + fiveLetterWordsAndWeightsMap.get(word));
					writer.newLine();
				}

				writer.close();

			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		/////////////////////////////////////////

		else
		{
			// file already exists, load contents into memory
			try {
				// Open the file of all English words
				//FileInputStream fstream = new FileInputStream(fiveLetterWordsAndWeightsFilePathName);
				//InputStream istream = new ClassPathResource(fiveLetterWordsAndWeightsFilePathName).getInputStream();


				DataInputStream in = new DataInputStream(istream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));

				String strLine;

				// Read File Line By Line and store in fiveLetterWordsAndWeights
				while ((strLine = br.readLine()) != null) {
					// split into word and weight tokens
					String[] tokens = strLine.split(ENTRY_DELIMITER); // 0 = word, 1 = weight
					fiveLetterWordsAndWeightsMap.put(tokens[0], Float.valueOf(tokens[1]));
				}

				// Close the input stream
				in.close();
			} catch (Exception e) { // Catch exception if any
				e.printStackTrace();
			}

		}
	}

	/**
	 * Load all 5 letter words from word list. Exclude words with non-alphabetic characters.
	 * 
	 */
	private static void loadFiveLetterWords() {
		try {
			// Open the file of all English words
			FileInputStream fstream = new FileInputStream(englishWordsFilePathName);

			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String word;

			// Read File Line By Line and save 5 letter words to fiveLetterWordsList
			while ((word = br.readLine()) != null) {
				if (word.length() == 5) {
					// exclude words with non-alphabetic characters
					if (!containsNonAlpha(word))
					{
						fiveLetterWordsList.add(word);
					}
				}
			}

			// Close the input stream
			in.close();
		} catch (Exception e) { // Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	/**
	 * Determines if the given letter has a black occurrence, based on current guess and result values.
	 * 
	 * @param letter
	 * @return true is letter has black occurrence; false otherwise
	 */
	private boolean hasBlack(char letter)
	{
		boolean hasBlack = false;

		for (int i = 0; i < guess.length(); i++) 
		{
			if (guess.charAt(i) == letter)
			{
				if (result.charAt(i) == blackChar)
				{
					hasBlack = true;
					break;
				}
			}

		}

		return hasBlack;
	}

	/**
	 * Determines the total number of yellow and green occurrences of a given letter, based on current guess and result values.
	 * @param letter
	 * @return total number of yellow and green occurrences of letter
	 */
	private int countOfYellowAndGreen(char letter)
	{
		int count = 0;

		for (int i = 0; i < guess.length(); i++) 
		{
			if (guess.charAt(i) == letter)
			{
				if ((result.charAt(i) == yellowChar) || (result.charAt(i) == greenChar))
				{
					count++;
				}
			}

		}

		return count;		
	}

	/**
	 * Determines if all letters in result are either green or yellow (i.e, none are black)
	 * 
	 * @return true if all letters in result are either green or yellow
	 */
	private boolean allLettersAreGreenOrYellow()
	{
		boolean allGreenAndYellow = true;

		for (int i = 0; i < result.length(); i++) 
		{
			if (result.charAt(i) == blackChar)
			{
				allGreenAndYellow = false;
				break;
			}
		}

		// set global var
		allLettersAreGreenOrYellow = allGreenAndYellow;

		return allGreenAndYellow;

	}

	/**
	 * Determine if the word satisfies all rules. 
	 * Examine the rules in order. If a rule is not satisfied, return false without checking the remainder of the rules.
	 * @param word the word to check
	 * @return true if satisfies all rules; false otherwise
	 */
	private boolean wordSatisfiesAllRules(String word)
	{
		// RULE 1: positions where we know the letter that goes there
		for (int i = 0; i < positionsWithRightLetter.length; i++) 
		{
			if (positionsWithRightLetter[i] != '-')
			{
				if (positionsWithRightLetter[i] != word.charAt(i))
				{
					// rule is violated
					return false;
				}
			}
		}


		// RULE 2: positions where we know what letters do NOT go there (but do not know which letter does go there)	
		for (int i = 0; i < positionsWithWrongLetters.length; i++) 
		{
			// Fail this rule check if any letter in positionsWithWrongLetters[i] is present in the word in given position
			for (Iterator<Character> iterator = positionsWithWrongLetters[i].iterator(); iterator.hasNext();) 
			{
				char wrongLetter = iterator.next();
				if (word.charAt(i) == wrongLetter)
				{
					// rule is violated
					return false;
				}
			}			
		}

		// RULE 3: letters for which we know exact number of occurrences
		for (Entry<Character, Integer> entry : lettersWithExactOccurrencesKnown.entrySet()) 
		{
			Character character = entry.getKey();
			Integer count = entry.getValue();

			if (count != getCharCountInString(word, character))
			{
				// rule is violated
				return false;
			}			
		}		

		// RULE 4: letters for which we know a non-zero min # of occurrences, but do not know the exact #		
		for (Entry<Character, Integer> entry : lettersWithMinButNotExactOccurrencesKnown.entrySet()) 
		{
			Character character = entry.getKey();
			Integer minCount = entry.getValue();

			if (!(minCount <= getCharCountInString(word, character)))
			{
				// rule is violated
				return false;
			}			
		}	

		// if get here, then all rules are satisfied
		return true;
	}

	/**
	 * Determines the number of occurrences of a character in a string
	 * @param string
	 * @param c
	 * @return
	 */
	private int getCharCountInString(String string, char c)
	{
		int count = 0;

		for (int i = 0; i < string.length(); i++) 
		{
			if (string.charAt(i) == c)
			{
				count++;
			}

		}

		return count;
	}

	/**
	 * Determines the weight of a word based on:
	 * 	1. the frequency of each letter in English words, using first letter frequency weight for first letter in word
	 * 	2. the existence of repeated letter (reduces weight)

	 * 
	 * Formula:
	 * 
	 * 	- 1) First, consider frequency of each letter appearing in English words
	 * 		- the frequency weight of a letter is it's frequency percentage
	 * 		- example: 'E' frequency is 11%, so it's frequency weight = 11
	 * 		- for the first letter in the word, use the first letter frequency 
	 * 		- word weight = sum of frequency weight of each letter in word
	 * 	- 2) Next, consider existence of repeated letters (reduces weight)
	 * 		- each occurrence of a letter after the first occurrence shall get a weight of 1/4 it's frequency percentage
	 * 		- so, we need to adjust the word weight from #1 for these multiple occurrences
	 * 		- for EACH letter with multiple occurrences, *REDUCE* the word weight by this amount:
	 * 			- (total # of occurrences of letter - 1) * letter frequency percentage * 3/4
	 * 		- example: 'E' occurs 2 times in guess word
	 * 			- the first 'E' gets a weight of 11, while the second 'E' get a weight of 11/4
	 * 		- notes: 
	 * 			- no need to consider lettersWithMinButNotExactOccurrencesKnown since all words would get the same adjustment (so why bother)
	 * 			- need to consider letters in lettersWithExactOccurrencesKnown since the guess words will not exceed those letters
	 * 
	 * Reference: https://en.wikipedia.org/wiki/Letter_frequency#Relative_frequencies_of_letters_in_the_English_language
	 * 
	 * @param word
	 * @return
	 */
	private static float weighWord(String word)
	{
		float wordWeight = 0;

		// 1) First, consider weight of first letter using the first letter frequency
		wordWeight = wordWeight + firstLetterFrequenciesMap.get(word.charAt(0));

		// 2) Next, consider frequency of remaining letters		
		for (int i = 1; i < word.length(); i++) 
		{
			// Get frequency of letter
			wordWeight = wordWeight + letterFrequenciesMap.get(word.charAt(i));
		}

		// 3) Next, consider existence of repeated letters (reduces weight)
		List<Character> lettersAlreadyOccured = new ArrayList<Character>();
		for (int i = 0; i < word.length(); i++) 
		{
			if (lettersAlreadyOccured.contains(word.charAt(i))) {
				// letter has already occurred in word, subtract 3/4 of it's letter frequency weight
				wordWeight = wordWeight - (letterFrequenciesMap.get(word.charAt(i)) * 3/4);
			}
			lettersAlreadyOccured.add(word.charAt(i));
		}

		return wordWeight;

	}

	private static boolean containsNonAlpha(String s) {
		return s.matches(".*[^a-zA-Z].*");
	}

	/**
	 * Comparator used for sorting entries in fiveLetterWordsAndWeightsMap
	 *
	 */
	@SuppressWarnings("unused")
	private class ValueComparator implements Comparator<String> {
		@Override
		public int compare(String s1, String s2) 
		{
			int result = 0;
			if (fiveLetterWordsAndWeightsMap.get(s1) == null)
			{
				result = 0;
			}
			else
			{
				result = fiveLetterWordsAndWeightsMap.get(s1).compareTo(fiveLetterWordsAndWeightsMap.get(s2));
			}

			return result;
		}
	}


	private void deleteDirectory(File file) {
	
	    File[] list = file.listFiles();
	    if (list != null) {
	        for (File temp : list) {
	            //recursive delete
	            System.out.println("Visit " + temp);
	            deleteDirectory(temp);
	        }
	    }
	
	    if (file.delete()) {
	        System.out.printf("Delete : %s%n", file);
	    } else {
	        System.err.printf("Unable to delete file or directory : %s%n", file);
	    }
	
	}
	
// NOT NEEDED	
//	private void init()
//	{		
//		// Construct serialized session dir
//		serializedSessionDir = serializedBaseDir + sessionId + "/";
//		
//		// Read in serialized data
//		deserializeState();
//	}
	
	private void printMap(Map<Character, Integer> map)
	{
		System.out.println("Printing Map..");

        Set<Entry<Character, Integer>> set = map.entrySet(); 
        Iterator<Entry<Character, Integer>> iterator = set.iterator(); 
  
        while (iterator.hasNext()) { 
            Entry<Character, Integer> entry = iterator.next(); 
  
            System.out.print("key : " + entry.getKey() + " & Value : "); 
            System.out.println(entry.getValue()); 
        } 
	}
}

	