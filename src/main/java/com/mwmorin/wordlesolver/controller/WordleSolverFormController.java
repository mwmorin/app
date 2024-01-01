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

            // Delete session files if sessionId is set
            if (!StringUtils.isEmpty(guess.getSessionId()))
            {
                // Delete state for given sessionId
                WordleSolverUtility wordleSolverUtility = new WordleSolverUtility(guess.getSessionId());
                wordleSolverUtility.deleteAllSerializedSessionFiles();
            }

            // Clear the form
            guess = new Guess();
            model.addAttribute("guess", guess);
        }
        else {
            // Submit button clicked. Determine best next word to guess.

            // Create sessionId if none set
            String sessionId = guess.getSessionId();
            if (StringUtils.isEmpty(sessionId)) {
                // Create sessionId
                UUID uuid = UUID.randomUUID();
                sessionId = uuid.toString();
                guess.setSessionId(sessionId);

                System.out.println("No sessionId set, so created sessionId: " + sessionId);
            }
            else {
                System.out.println("SessionId passed in: " + sessionId);
            }

            // Determine best next word to guess.
            WordleSolverUtility wordleSolverUtility = new WordleSolverUtility(guess.getSessionId());
            String nextword = wordleSolverUtility.getNextGuess(guess.getWordGuessed(), guess.getResult());

            // Set next word on model
            guess.setNextBestWordToGuess(nextword);
            model.addAttribute("guess", guess);
        }

        // return submitresult.html template populated with model
        return "submitresult";
    }

}

