package com.example;

class StringUtils {

    static String padLeft(String input, int size) {
        return input.length() < size ? padLeft(" " + input, size) : input;
    }

    static String padRight(String input, int size) {
        return input.length() < size ? padRight(input + " ", size) : input;
    }
}
