package com.mwmorin.wordlesolver.model;

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
}
