package io.skippy.gradle.core;

import io.skippy.junit5.Skippy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(Skippy.class)
public class SourceFileTest1 {
    @Test
    void testSomething() {
        assertEquals("hello", "hello");
    }
}
