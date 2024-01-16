package com.mwmorin.wordlesolver.util;

import java.util.Map;

public class PrintUtility {

    public static void print(String className, String message)
    {
        System.out.println(className + ": " + message);
    }

    public static void printMethod(String methodName)
    {
        System.out.println("Method called: " + methodName);
    }

    public static void printMethod(String methodName, String httpVerb)
    {
        System.out.println("Method called: " + methodName + ", HTTP verb: " + httpVerb);
    }

    public static void printQueryParams(Map<String, String> qparams) {

        System.out.println("Query params:");
        qparams.forEach((a,b) -> {
            System.out.println(String.format("\t%s -> %s",a,b));
        });

    }
}
