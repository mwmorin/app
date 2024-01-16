package com.mwmorin.wordlesolver.model;

import java.io.Serializable;

/**
 * Attributes of a single guess.
 */
public class Guess implements Serializable {
    private static final long serialVersionUID = 1L;

    private String wordGuessed;
    private int guessNumber = 0;
    private String result;

    public String getWordGuessed() {
        return wordGuessed;
    }

    public Guess(String wordGuessed, int guessNumber, String result) {
        setWordGuessed(toUpper(wordGuessed));
        setResult(toUpper(result));
        setGuessNumber(guessNumber);
    }

    public void setWordGuessed(String wordGuessed) {
        this.wordGuessed = toUpper(wordGuessed);
    }

    public int getGuessNumber() {
        return guessNumber;
    }

    public void setGuessNumber(int guessNumber) {
        this.guessNumber = guessNumber;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = toUpper(result);
    }

    public String toString()
    {
        String toStr = this.getClass().getName() + ": ["
                + "guessNumber = " + guessNumber + ", "
                + "wordGuessed = " + wordGuessed + ", "
                + "result = " + result
                + "]";

        return toStr;
    }

    /**
     * Returns given input string on all upper case letters
     * @param inputStr
     * @return
     */
    private String toUpper(String inputStr)
    {
        String upperStr = inputStr;
        if (inputStr != null)
        {
            upperStr = inputStr.toUpperCase();
        }

        return upperStr;
    }
}
