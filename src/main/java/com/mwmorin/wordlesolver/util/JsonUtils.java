package com.mwmorin.wordlesolver.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {

    /**
     * Convert given object into a JSON string.
     *
     * @param inputObject
     * @return
     */
    public static String objectToJson(Object inputObject)
    {
        String jsonStr = null;
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            jsonStr =  objectMapper.writeValueAsString(inputObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return jsonStr;
    }
}
