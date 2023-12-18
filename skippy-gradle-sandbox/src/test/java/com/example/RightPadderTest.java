package com.example;

import io.skippy.junit5.Skippified;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Skippified
public class RightPadderTest {

    @Test
    void testPadLeft() {
        var input = TestConstants.HELLO;
        assertEquals("hello ", RightPadder.padRight(input, 6));
        assertEquals("hello ", RightPadder.padRight(input, 6));
    }

}
