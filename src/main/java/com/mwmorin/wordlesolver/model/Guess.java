package com.mwmorin.wordlesolver.model;


public class Guess {

    private String wordGuessed;
    private String result;
    private String nextBestWordToGuess;
    private String sessionId;
    private String solution;
    private boolean requestIsValid = true; // true by default
    private String autofocusOnResultField = "";
    private String errorMessage;

    public String getWordGuessed() {
        return wordGuessed;
    }

    public void setWordGuessed(String wordGuessed) {
        this.wordGuessed = formatInput(wordGuessed);
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
