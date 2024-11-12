package com.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LeftPadderTest {

    @Test
    void testPadLeft() {
        String input = "hello";
        Assertions.assertEquals(" hello", LeftPadder.padLeft(input, 6));
    }
}
