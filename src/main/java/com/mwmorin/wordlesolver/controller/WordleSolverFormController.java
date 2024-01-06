package com.mwmorin.wordlesolver.controller;

import com.mwmorin.wordlesolver.model.Greeting;
import com.mwmorin.wordlesolver.model.Guess;
import com.mwmorin.wordlesolver.util.WordleSolverUtility;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
public class WordleSolverFormController {

    @GetMapping("/greetingformtest")
    public String greetingForm(Model model) {
        model.addAttribute("greeting", new Greeting());
        return "greeting";
    }

    @PostMapping("/greetingformtest")
    public String greetingSubmit(@ModelAttribute Greeting greeting, Model model) {
        model.addAttribute("greeting", greeting);
        return "result";
    }

    @GetMapping("/getnextwordui")
    public String getNextWordUI(Model model) {
        model.addAttribute("guess", new Guess());
        return "submitresult";
    }

    @PostMapping("/getnextwordui")
    public String greetingSubmit(@ModelAttribute Guess guess, Model model, @RequestParam(value="action", required=true) String action) {

        System.out.println("==>> Action is: " + action);

        // Take action based on button clicked
        if (action.equalsIgnoreCase("Reset"))
        {
            // Reset button clicked. Delete session files and clear the form.

            guess = reset(guess.getSessionId());
            model.addAttribute("guess", guess);
        }
        else {
            // Submit button clicked. If puzzle is solved, reset form and add solution word. Otherwise, determine best next word to guess.

            if (StringUtils.isEmpty(guess.getWordGuessed()) || StringUtils.isEmpty(guess.getResult()))
            {
                // Validation failed. Specify that on form.
                guess.setRequestIsValid(false);
            }
            else if ("ggggg".equals(guess.getResult()))
            {
                // Puzzle solved! Reset form, then add solution.
                String solution = guess.getWordGuessed(); // Solution is word guessed
                guess = reset(guess.getSessionId());
                guess.setSolution(solution);
            }
            else {

                // Puzzle is not solved. Get best next guess.

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

                // Set next word on model
                guess.setNextBestWordToGuess(nextword);
                guess.setWordGuessed(nextword); // Populate suggested word to guess as best next guess
                guess.setResult(null); // Clear the result after each guess
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

}

