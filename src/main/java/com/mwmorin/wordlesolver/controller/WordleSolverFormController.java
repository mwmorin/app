package com.mwmorin.wordlesolver.controller;

import com.mwmorin.wordlesolver.model.Greeting;
import com.mwmorin.wordlesolver.model.Guess;
import com.mwmorin.wordlesolver.util.PrintUtility;
import com.mwmorin.wordlesolver.util.WordleSolverUtility;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@CrossOrigin(origins = "*")
@Controller
public class WordleSolverFormController {

    private static String className = WordleSolverFormController.class.getName();

    /**
     * Enable CORS by handling preflight check
     * @return
     */
//    @RequestMapping(
//            value = "/**",
////            value = "/preflightmike",
//            method = RequestMethod.OPTIONS
//    )
//    public ResponseEntity handle() {
//
//        System.out.println("===>>> Preflight called!!!!!!!!!");
//        ResponseEntity re = new ResponseEntity(HttpStatus.OK);
//
//        return new ResponseEntity(HttpStatus.OK);
//    }

    @GetMapping("/greetingformtest")
    public String greetingForm(Model model) {

        // Debug - print method name called
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        PrintUtility.printMethod(methodName);

        model.addAttribute("greeting", new Greeting());
        return "greeting";
    }

    @PostMapping("/greetingformtest")
    public String greetingSubmit(@ModelAttribute Greeting greeting, Model model) {

        // Debug - print method name called
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        PrintUtility.printMethod(methodName);

        model.addAttribute("greeting", greeting);
        return "result";
    }

    @GetMapping(value = {"/getnextwordui", "/"})
    public String getNextWordUI(Model model) {

        // Debug - print method name called
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        PrintUtility.printMethod(methodName);

        model.addAttribute("guess", new Guess());
        return "submitresult";
    }

    @PostMapping(value = {"/getnextwordui", "/"})
    public String greetingSubmit(@ModelAttribute Guess guess, Model model, @RequestParam(value="action", required=true) String action) {

        // Debug - print method name called
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        PrintUtility.printMethod(methodName);

        System.out.println("greetingSubmit() called with: "); // TODO - change method name to getNextWordUI and change this print string
        System.out.println("==>> Action is: " + action);
        System.out.println("==>> Guess is: " + guess.toString());
        System.out.println("==>> Guess #: " + guess.getGuessNumber());

        // Take action based on button clicked
        // RESET
        if (action.equalsIgnoreCase("Reset"))
        {
            // Reset button clicked. Delete session files and clear the form.

            guess = reset(guess.getSessionId());
            model.addAttribute("guess", guess);
        }
        // SUBMIT
        else {
            // Submit button clicked. If puzzle is solved, reset form and add solution word. Otherwise, determine best next word to guess.

            if (!validate(guess))
            {
                // Form has invalid input. Simply return form model below (which is already set with validation results).
            }
            else if ("ggggg".equalsIgnoreCase(guess.getResult()))
            {
                // Puzzle solved! Reset form, then add solution.
                String solution = guess.getWordGuessed(); // Solution is word guessed
                int guessesToSolve = guess.getGuessNumber();
                guess = reset(guess.getSessionId());
                guess.setSolution(solution);
                guess.setGuessesToSolve(guessesToSolve);
            }
            else {

                // Puzzle is not solved. Get best next guess.

                // Set autofocus to result field
                guess.setAutofocusOnResultField("autofocus");

                // Create sessionId if none set
                String sessionId = guess.getSessionId();
                if (StringUtils.isEmpty(sessionId)) {
                    // Create sessionId
                    UUID uuid = UUID.randomUUID();
                    sessionId = uuid.toString();
                    guess.setSessionId(sessionId);

                    System.out.println("No sessionId set, so created sessionId: " + sessionId);
                } else {
                    System.out.println("SessionId passed in: " + sessionId);
                }

                // Determine best next word to guess.
                WordleSolverUtility wordleSolverUtility = new WordleSolverUtility(guess.getSessionId());
                String nextword = wordleSolverUtility.getNextGuess(guess.getWordGuessed(), guess.getResult());

                // Process answer
                if (!StringUtils.isEmpty(nextword))
                {
                    // Next word was found. Set it on model.
                    System.out.println("Next best word to guess was found: " + nextword);
                    guess.setNextBestWordToGuess(nextword);
                    guess.setWordGuessed(nextword); // Populate suggested word to guess as best next guess
                    guess.setResult(null); // Clear the result after each guess
                    guess.setGuessNumber(guess.getGuessNumber() + 1); // increment guess number after each guess
                }
                else
                {
                    // Next word NOT found. likely result was typed in wrong. Reset form then add error message.
                    System.err.println("Next best word to guess was NOT found.");
                    guess = reset(guess.getSessionId());
                    guess.setErrorMessage("Error: No word found. Please ensure result is correct. Game has reset.");
                }

            }

        }

        // return submitresult.html template populated with model
        model.addAttribute("guess", guess);
        return "submitresult";
    }

    /**
     * Delete session files and clear the form
     * @param sessionId
     * @return cleared form
     */
    private Guess reset(String sessionId)
    {
        // Delete session files if sessionId is set
        if (!StringUtils.isEmpty(sessionId))
        {
            // Delete state for given sessionId
            WordleSolverUtility wordleSolverUtility = new WordleSolverUtility(sessionId);
            wordleSolverUtility.deleteAllSerializedSessionFiles();
        }

        return new Guess();
    }

    /**
     * Determines if the form values (populated on the Guess instance) are valid, and set validation results on form.
     *
     * Validation rules:
     * 1. wordGuess must contain exactly 5 chars
     * 2. result must contain exactly 5 chars
     *
     * @param guess
     * @return true if valid; false otherwise
     */
    private boolean validate(Guess guess)
    {
        boolean isValid = true;
        if (guess == null
                || StringUtils.isEmpty(guess.getWordGuessed())  || guess.getWordGuessed().length() != 5
                || StringUtils.isEmpty(guess.getResult()) || guess.getResult().length() != 5
            )
        {
            // Not valid
            guess.setRequestIsValid(false);
            guess.setErrorMessage("Invalid input: Please enter a 5 letter word and 5 letter result!");
            isValid = false;
        }
        else
        {
            // Is valid
            guess.setRequestIsValid(true);
        }

        return isValid;
    }

}

