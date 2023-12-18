package com.example;

import io.skippy.junit5.Skippified;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Skippified
public class SkippifiedStringUtilsTest {

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