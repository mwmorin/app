package com.mwmorin.wordlesolver.controller;

import com.mwmorin.wordlesolver.model.Greeting;
import com.mwmorin.wordlesolver.model.Guess;
import com.mwmorin.wordlesolver.util.WordleSolverUtility;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

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
    public String greetingSubmit(@ModelAttribute Guess guess, Model model) {

        // Determine best next word to guess
        WordleSolverUtility wordleSolverUtility = new WordleSolverUtility(guess.getSessionId());
        String nextword = wordleSolverUtility.getNextGuess(guess.getWordGuessed(), guess.getResult());

        // Set next word on model
        guess.setNextBestWordToGuess(nextword);
        model.addAttribute("guess", guess);

        // return submitresult.html template populated with model
        return "submitresult";
    }

}

