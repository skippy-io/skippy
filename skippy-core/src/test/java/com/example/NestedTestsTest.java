package com.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class NestedTestsTest {

    @Test
    void testSomething() {
        Assertions.assertEquals("helloA", ClassA.append("hello"));
    }

    @Nested
    class Level2BarTest {
        Level2BarTest() {
        }

        @Test
        void testSomething() {
            Assertions.assertEquals("helloD", ClassD.append("hello"));
        }
    }

    @Nested
    class Level2FooTest {
        Level2FooTest() {
        }

        @Test
        void testSomething() {
            Assertions.assertEquals("helloB", ClassB.append("hello"));
        }

        @Nested
        class Level3Test {
            Level3Test() {
            }

            @Test
            void testSomething() {
                Assertions.assertEquals("helloC", ClassC.append("hello"));
            }
        }
    }
}
