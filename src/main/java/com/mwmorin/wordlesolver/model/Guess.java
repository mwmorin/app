package com.mwmorin.wordlesolver.model;


import org.thymeleaf.util.StringUtils;

import java.util.Locale;

public class Guess {

    private String wordGuessed;
    private String result;
    private String nextBestWordToGuess;
    private String sessionId;
    private String solution;
    private boolean requestIsValid = true; // true by default

    public String getWordGuessed() {
        return wordGuessed;
    }

    public void setWordGuessed(String wordGuessed) {
        this.wordGuessed = toLowerCase(wordGuessed);
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = toLowerCase(result);
    }

    public String getNextBestWordToGuess() {
        return nextBestWordToGuess;
    }

    public void setNextBestWordToGuess(String nextBestWordToGuess) {
        this.nextBestWordToGuess = toLowerCase(nextBestWordToGuess);
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public boolean isRequestIsValid() {
        return requestIsValid;
    }

    public void setRequestIsValid(boolean requestIsValid) {
        this.requestIsValid = requestIsValid;
    }

    private String toLowerCase(String inputStr)
    {
        return StringUtils.toLowerCase(inputStr, Locale.getDefault());
    }

    public void clearAll()
    {
        this.wordGuessed = null;
        this.result = null;
        this.nextBestWordToGuess = null;
        this.sessionId = null;
        this.solution = null;
        this.requestIsValid = true;
    }
}
