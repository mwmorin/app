package com.mwmorin.wordlesolver.model;

import java.util.ArrayList;
import java.util.List;

public class GetNextGuessResponse {

    /*
   	- word (next best word to guess)
	- sessionId (if none set on request, then wordleutily shall generate)
	- isRequestValid ??
	- errorMessage (any error message)
     */

    /**
     * Next best word to guess
     */
    private String word;
    private String sessionId;
    private boolean isRequestValid = true;
    private String errorMessage;
    private List<Guess> guessHistory = new ArrayList<Guess>();
    private boolean isSolved = false;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isRequestValid() {
        return isRequestValid;
    }

    public void setRequestValid(boolean requestValid) {
        isRequestValid = requestValid;
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

    public void setGuessHistory(List<Guess> guessHistory) {
        this.guessHistory = guessHistory;
    }

    public boolean isSolved() {
        return isSolved;
    }

    public void setSolved(boolean solved) {
        isSolved = solved;
    }
}
