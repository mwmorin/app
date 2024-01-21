package com.mwmorin.wordlesolver.controller;

import com.mwmorin.wordlesolver.model.GetNextGuessResponse;
import com.mwmorin.wordlesolver.model.Greeting;
import com.mwmorin.wordlesolver.model.GuessDetails;
import com.mwmorin.wordlesolver.util.PrintUtility;
import com.mwmorin.wordlesolver.util.WordleSolverUtility;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@CrossOrigin(origins = "*")
@Controller
public class WordleSolverFormController {

    private static String className = WordleSolverFormController.class.getName();

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
        PrintUtility.printMethod(methodName, HttpMethod.GET.name());

        model.addAttribute("guessDetails", new GuessDetails());
        return "submitresult";
    }

    @PostMapping(value = {"/getnextwordui", "/"})
    public String getNextWordUI(@ModelAttribute GuessDetails guessDetails, Model model, @RequestParam(value="action", required=true) String action) {

        // Debug - print method name called
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        PrintUtility.printMethod(methodName, HttpMethod.POST.name());
        System.out.println("\tAction is: " + action);
        System.out.println("\tGuessDetails object is: " + guessDetails.toString());

        // Take action based on button clicked
        // RESET clicked
        if (action.equalsIgnoreCase("Reset"))
        {
            // Reset button clicked. Delete session files and clear the form.

            guessDetails = reset(guessDetails.getSessionId());
            model.addAttribute("guessDetails", guessDetails);
        }
        // SUBMIT clicked
        else {
            // Submit button clicked. Validate input and if valid, determine best next word to guess.

            if (!validate(guessDetails))
            {
                // Form has invalid input. Simply return form model below (which is already set with validation results).
            }
            else {

                // Input is valid. Get best next guess.

                // TODO - MIKE - move this down and do this only if NOT sovled
                // Set autofocus to result field
//                guessDetails.setAutofocusOnResultField("autofocus");

                // Create sessionId if none set
                String sessionId = guessDetails.getSessionId();
                if (StringUtils.isEmpty(sessionId)) {
                    // Create sessionId
                    UUID uuid = UUID.randomUUID();
                    sessionId = uuid.toString();
                    guessDetails.setSessionId(sessionId);

                    System.out.println("No sessionId set, so created sessionId: " + sessionId);
                } else {
                    System.out.println("SessionId passed in: " + sessionId);
                }

                // Determine best next word to guess.
                WordleSolverUtility wordleSolverUtility = new WordleSolverUtility(guessDetails.getSessionId());
                GetNextGuessResponse getNextGuessResponse = wordleSolverUtility.getNextGuess(guessDetails.getWordGuessed(), guessDetails.getResult());

                String nextword = getNextGuessResponse.getWord();

                // Process results
                if (getNextGuessResponse.isRequestValid())
                {
                    // Request is valid

                    if (getNextGuessResponse.isSolved())
                    {
                        // Puzzle solved! Reset form, then add solution.
                        System.out.println("Puzzle is solved! Answer is: " + nextword);
                        String solution = guessDetails.getWordGuessed(); // Solution is word guessed
                        int guessesToSolve = guessDetails.getGuessNumber();
                        guessDetails = reset(guessDetails.getSessionId());
                        guessDetails.setSolution(solution);
                        guessDetails.setGuessesToSolve(guessesToSolve);
                        guessDetails.setGuessHistory(getNextGuessResponse.getGuessHistory());
                    }
                    else
                    {
                        // Puzzle is not solved. Add results to model
                        System.out.println("Next best word to guess was found: " + nextword);
                        guessDetails.setNextBestWordToGuess(nextword);
                        guessDetails.setWordGuessed(nextword); // Populate suggested word to guess as best next guess
                        guessDetails.setResult(null); // Clear the result after each guess
                        guessDetails.setGuessNumber(guessDetails.getGuessNumber() + 1); // increment guess number after each guess
                        guessDetails.setGuessHistory(getNextGuessResponse.getGuessHistory());
                        // Set autofocus to result field
                        guessDetails.setAutofocusOnResultField("autofocus");
                    }
                }
                else
                {
                    // Request is not valid.
                    System.err.println("Request is not valid.");
                    guessDetails = reset(guessDetails.getSessionId());
                    guessDetails.setErrorMessage(getNextGuessResponse.getErrorMessage() + " Game has reset.");
                    guessDetails.setGuessHistory(getNextGuessResponse.getGuessHistory());
                }
            }
        }

        // return submitresult.html template populated with model
        model.addAttribute("guessDetails", guessDetails);
        return "submitresult";
    }

    /**
     * Delete session files and clear the form
     * @param sessionId
     * @return cleared form
     */
    private GuessDetails reset(String sessionId)
    {
        // Delete session files if sessionId is set
        if (!StringUtils.isEmpty(sessionId))
        {
            // Delete state for given sessionId
            WordleSolverUtility wordleSolverUtility = new WordleSolverUtility(sessionId);
            wordleSolverUtility.deleteAllSerializedSessionFiles();
        }

        return new GuessDetails();
    }

    /**
     * Determines if the form values (populated on the Guess instance) are valid, and set validation results on form.
     *
     * Validation rules:
     * 1. wordGuess must contain exactly 5 chars
     * 2. result must contain exactly 5 chars
     *
     * @param guessDetails
     * @return true if valid; false otherwise
     */
    private boolean validate(GuessDetails guessDetails)
    {
        boolean isValid = true;
        if (guessDetails == null
                || StringUtils.isEmpty(guessDetails.getWordGuessed())  || guessDetails.getWordGuessed().length() != 5
                || StringUtils.isEmpty(guessDetails.getResult()) || guessDetails.getResult().length() != 5
            )
        {
            // Not valid
            guessDetails.setRequestIsValid(false);
            guessDetails.setErrorMessage("Invalid input: Please enter a 5 letter word and 5 letter result!");
            isValid = false;
        }
        else
        {
            // Is valid
            guessDetails.setRequestIsValid(true);
        }

        return isValid;
    }

}

