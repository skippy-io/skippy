package com.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringUtilsTest {

    @Test
    void testPadLeft() {
        var input = TestConstants.HELLO;
        assertEquals(" hello", StringUtils.padLeft(input, 6));
    }

    @Test
    void testPadRight() {
        var input = TestConstants.HELLO;
        assertEquals("hello ", StringUtils.padRight(input, 6));
    }

}