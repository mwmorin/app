package com.mwmorin.wordlesolver.controller;

import com.mwmorin.wordlesolver.util.WordleSolverUtility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WordleSolverController {

    @Value("${wordlesolver.prop1}")
    private String prop1;

    @RequestMapping("/greeting")
    public String getGreeting() {
        return "Hi it's Mike!, prop1 value: " + prop1;
    }

    @RequestMapping(value = "/testpost", method = RequestMethod.POST)
    public String testFormPost(String sessionId, String guess, String result) {
        return "Hi from test testFormPost. You passed in params:\n "
                + "sessionId: " + sessionId + ", guess: " + guess + ", result: " + result;
    }

    @RequestMapping(value = "/getnextword", method = RequestMethod.POST)
    public String getNextWord(String sessionId, String guess, String result) {

        String nextword = null;

        // Instantiate WordleSolverUtility
        WordleSolverUtility wordleSolverUtility = new WordleSolverUtility(sessionId);

        nextword = wordleSolverUtility.getNextGuess(guess, result);

        return nextword;
    }
    @RequestMapping(value = "/cleanup", method = RequestMethod.DELETE)
    public void cleanupSession(String sessionId)
    {
        // Delete state for given sessionId
        WordleSolverUtility wordleSolverUtility = new WordleSolverUtility(sessionId);
        wordleSolverUtility.deleteAllSerializedSessionFiles();
    }

}
