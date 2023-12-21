package com.example;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringUtilsTest {

    @Test
    public void testPadLeft() {
        var input = TestConstants.HELLO;
        assertEquals(" hello", StringUtils.padLeft(input, 6));
    }

    @Test
    public void testPadRight() {
        var input = TestConstants.HELLO;
        assertEquals("hello ", StringUtils.padRight(input, 6));
    }

}