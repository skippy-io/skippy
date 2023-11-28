package io.skippy.gradle.core;

import io.skippy.junit5.Skippy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@org.junit.jupiter.api.extension.ExtendWith(Skippy.class)
public class SourceFileTest3 {
    @Test
    void testSomething() {
        assertEquals("hello", "hello");
    }
}
