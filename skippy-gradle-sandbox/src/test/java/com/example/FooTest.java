package com.example;

import io.skippy.junit5.Skippy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(Skippy.class)
public class FooTest {

    @Test
    void name() {
        assertTrue(true);
    }
}
