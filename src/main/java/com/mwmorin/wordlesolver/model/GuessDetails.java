package com.mwmorin.wordlesolver.model;


import java.util.ArrayList;
import java.util.List;

public class GuessDetails {

    private String wordGuessed;
    private int guessNumber = 1;
    private int guessesToSolve = 0;
    private String result;
    private String nextBestWordToGuess;
    private String sessionId;
    private String solution;
    private boolean requestIsValid = true; // true by default
    private String autofocusOnResultField = "";
    private String errorMessage;
//    private List<Guess> guessHistory = new ArrayList<Guess>();
    private List<Guess> guessHistory = null;

    public String getWordGuessed() {
        return wordGuessed;
    }

    public void setWordGuessed(String wordGuessed) {
        this.wordGuessed = formatInput(wordGuessed);
    }

    public int getGuessNumber() {
        return guessNumber;
    }

    public void setGuessNumber(int guessNumber) {
        this.guessNumber = guessNumber;
    }

    public int getGuessesToSolve() {
        return guessesToSolve;
    }

    public void setGuessesToSolve(int guessesToSolve) {
        this.guessesToSolve = guessesToSolve;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = formatInput(result);
    }

    public String getNextBestWordToGuess() {
        return nextBestWordToGuess;
    }

    public void setNextBestWordToGuess(String nextBestWordToGuess) {
        this.nextBestWordToGuess = formatInput(nextBestWordToGuess);
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

    public String getAutofocusOnResultField() {
        return autofocusOnResultField;
    }

    public void setAutofocusOnResultField(String autofocusOnResultField) {
        this.autofocusOnResultField = autofocusOnResultField;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<Guess> getGuessHistory() {
        return guessHistory;
    }

    public void addGuess(Guess guess){
        if (this.guessHistory == null)
        {
            this.guessHistory = new ArrayList<Guess>();
        }
        this.guessHistory.add(guess);
    }

    public void setGuessHistory(List<Guess> guessHistory) {
        this.guessHistory = guessHistory;
    }

    /**
     * Formats input field value by:
     * - stripping leading/trailing whitespace
     * - converting to all upper case letters
     * @param inputStr
     * @return
     */
    private String formatInput(String inputStr)
    {
        String upperStr = inputStr;
        if (inputStr != null)
        {
            upperStr = inputStr.strip().toUpperCase();
        }

        return upperStr;
    }

    public void clearAll()
    {
        this.wordGuessed = null;
        this.guessNumber = 1;
        this.guessesToSolve = 0;
        this.result = null;
        this.nextBestWordToGuess = null;
        this.sessionId = null;
        this.solution = null;
        this.requestIsValid = true;
        this.autofocusOnResultField = "";
        this.errorMessage = null;
    }

    public String toString()
    {
        String toStr = this.getClass().getName() + ": ["
                + "wordGuessed = " + wordGuessed + ", "
                + "guessNumber = " + guessNumber + ", "
                + "guessesToSolve = " + guessesToSolve + ", "
                + "result = " + result + ", "
                + "nextBestWordToGuess = " + nextBestWordToGuess + ", "
                + "sessionId = " + sessionId + ", "
                + "solution = " + solution + ", "
                + "requestIsValid = " + requestIsValid + ", "
                + "autofocusOnResultField = " + autofocusOnResultField + ", "
                + "errorMessage = " + errorMessage
                + "]";

        return toStr;
    }
}
