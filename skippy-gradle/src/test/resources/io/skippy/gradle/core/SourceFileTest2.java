package io.skippy.gradle.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(io.skippy.junit5.Skippy.class)
public class SourceFileTest2 {
    @Test
    void testSomething() {
        assertEquals("hello", "hello");
    }
}
