package com.mwmorin.wordlesolver.util;

public class PrintUtility {

    public static void print(String className, String message)
    {
        System.out.println(className + ": " + message);
    }

    public static void printMethod(String methodName)
    {
        System.out.println("Method called: " + methodName);
    }
}
