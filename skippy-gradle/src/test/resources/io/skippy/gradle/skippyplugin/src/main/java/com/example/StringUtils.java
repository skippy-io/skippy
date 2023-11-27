package com.example;

class StringUtils {

    /**
     * Pads a {@index input} string on the left with blanks until the size is equal or greater to  {@index size}.
     */
    static String padLeft(String input, int size) {
        if (input.length() < size) {
            return padLeft(" " + input, size);
        }
        return input;
    }

    /**
     * Pads a {@index input} string on the right with blanks until the size is equal or greater to {@index size}.
     */
    static String padRight(String input, int size) {
        if (input.length() < size) {
            return padRight(input + " ", size);
        }
        return input;
    }
}
