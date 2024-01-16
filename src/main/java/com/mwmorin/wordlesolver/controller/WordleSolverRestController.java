package com.mwmorin.wordlesolver.controller;

import com.mwmorin.wordlesolver.model.GetNextGuessResponse;
import com.mwmorin.wordlesolver.util.JsonUtils;
import com.mwmorin.wordlesolver.util.PrintUtility;
import com.mwmorin.wordlesolver.util.WordleSolverUtility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/rest")
public class WordleSolverRestController {

    private static String className = WordleSolverRestController.class.getName();

    @Value("${wordlesolver.prop1}")
    private String prop1;

    @RequestMapping("/greeting")
    public String getGreeting() {

        // Debug - print method name called
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        PrintUtility.printMethod(methodName);

        return "Hi it's Mike!, prop1 value: " + prop1;
    }

    @RequestMapping(value = "/testpost", method = RequestMethod.POST)
    public String testFormPost(String sessionId, String guess, String result) {

        // Debug - print method name called
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        PrintUtility.printMethod(methodName);

        return "Hi from test testFormPost. You passed in params:\n "
                + "sessionId: " + sessionId + ", guess: " + guess + ", result: " + result;
    }

//    @RequestMapping(value = "/getnextword", method = RequestMethod.POST)
//    public String getNextWord(String sessionId, String guess, String result) {
//
//        // Debug - print method name called
//        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
//        PrintUtility.printMethod(methodName);
//
//        String nextword = null;
//
//        // Instantiate WordleSolverUtility
//        WordleSolverUtility wordleSolverUtility = new WordleSolverUtility(sessionId);
//
//        nextword = wordleSolverUtility.getNextGuess(guess, result);
//
//        return nextword;
//    }

    @RequestMapping(value = "/getnextwordjson", method = RequestMethod.GET)
    public String getNextWordJSON(@RequestParam(required=false) Map<String,String> qparams) {

        // Debug - print method and query params
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        PrintUtility.printMethod(methodName);
        PrintUtility.printQueryParams(qparams);

        // Get query params
        String guess = qparams.get("guess");
        String result = qparams.get("result");
        String sessionId = qparams.get("sessionId");

        // Instantiate WordleSolverUtility
        WordleSolverUtility wordleSolverUtility = new WordleSolverUtility(sessionId);

        // Get the next word to guess
        GetNextGuessResponse getNextGuessResponse = wordleSolverUtility.getNextGuess(guess, result);

        // Convert response to JSON and return
        return JsonUtils.objectToJson(getNextGuessResponse);
    }

    @RequestMapping(value = "/getnextwordjson", method = RequestMethod.POST)
    public String getNextWordJSON(String sessionId, String guess, String result) {

        // Debug - print method name called
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        PrintUtility.printMethod(methodName);

        // Instantiate WordleSolverUtility
        WordleSolverUtility wordleSolverUtility = new WordleSolverUtility(sessionId);

        // Get the next word to guess
        GetNextGuessResponse getNextGuessResponse = wordleSolverUtility.getNextGuess(guess, result);

        // Convert response to JSON and return
        return JsonUtils.objectToJson(getNextGuessResponse);
    }
    @RequestMapping(value = "/cleanup", method = RequestMethod.DELETE)
    public void cleanupSession(String sessionId)
    {

        // Debug - print method name called
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        PrintUtility.printMethod(methodName);

        // Delete state for given sessionId
        WordleSolverUtility wordleSolverUtility = new WordleSolverUtility(sessionId);
        wordleSolverUtility.deleteAllSerializedSessionFiles();
    }

}
