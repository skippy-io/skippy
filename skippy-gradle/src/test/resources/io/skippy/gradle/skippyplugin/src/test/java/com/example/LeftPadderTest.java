package com.example;

import io.skippy.junit5.Skippy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(Skippy.class)
public class LeftPadderTest {

    @Test
    void testPadLeft() {
        var input = TestConstants.HELLO;
        assertEquals(" hello", LeftPadder.padLeft(input, 6));
    }

}
