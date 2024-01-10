package com.mwmorin.wordlesolver.controller;

import com.mwmorin.wordlesolver.model.GetNextGuessResponse;
import com.mwmorin.wordlesolver.util.JsonUtils;
import com.mwmorin.wordlesolver.util.WordleSolverUtility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

//@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.POST, RequestMethod.DELETE, RequestMethod.GET})
@RestController
public class WordleSolverRestController {

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

    @RequestMapping(value = "/getnextwordjson", method = RequestMethod.POST)
    public String getNextWordJSON(String sessionId, String guess, String result) {

        // Instantiate WordleSolverUtility
        WordleSolverUtility wordleSolverUtility = new WordleSolverUtility(sessionId);

        // Get the next word to guess
        GetNextGuessResponse getNextGuessResponse = wordleSolverUtility.getNextGuessJSON(guess, result);

        // Convert response to JSON and return
        return JsonUtils.objectToJson(getNextGuessResponse);
    }
    @RequestMapping(value = "/cleanup", method = RequestMethod.DELETE)
    public void cleanupSession(String sessionId)
    {
        // Delete state for given sessionId
        WordleSolverUtility wordleSolverUtility = new WordleSolverUtility(sessionId);
        wordleSolverUtility.deleteAllSerializedSessionFiles();
    }

}
