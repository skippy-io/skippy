package com.example;

import io.skippy.junit4.Skippy;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertEquals;

public class SkippifiedStringUtilsTest {

    @ClassRule
    public static TestRule skippyRule = Skippy.skippify();

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