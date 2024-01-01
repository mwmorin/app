package com.mwmorin.wordlesolver.model;

public class Guess {

    private String wordGuessed;
    private String result;
    private String nextBestWordToGuess;
    private String sessionId;

    public String getWordGuessed() {
        return wordGuessed;
    }

    public void setWordGuessed(String wordGuessed) {
        this.wordGuessed = wordGuessed;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getNextBestWordToGuess() {
        return nextBestWordToGuess;
    }

    public void setNextBestWordToGuess(String nextBestWordToGuess) {
        this.nextBestWordToGuess = nextBestWordToGuess;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
