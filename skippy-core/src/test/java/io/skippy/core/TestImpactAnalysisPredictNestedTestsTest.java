/*
 * Copyright 2023-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.skippy.core;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static io.skippy.core.Prediction.EXECUTE;
import static io.skippy.core.Prediction.SKIP;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestImpactAnalysisPredictNestedTestsTest {

    @Test
    void testPredictNoChange() throws ClassNotFoundException {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "id": "F87679D196B903DFE24335295E13DEED",
                "classes": {
                    "0": {
                        "name": "com.example.ClassA",
                        "path": "com/example/ClassA.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "3041DC84"
                    },
                    "1": {
                        "name": "com.example.ClassB",
                        "path": "com/example/ClassB.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "37FA6DE7"
                    },
                    "2": {
                        "name": "com.example.ClassC",
                        "path": "com/example/ClassC.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "B17DF734"
                    },
                    "3": {
                        "name": "com.example.ClassD",
                        "path": "com/example/ClassD.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "41C10CEE"
                    },
                    "4": {
                        "name": "com.example.NestedTestsTest",
                        "path": "com/example/NestedTestsTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "7797EB55"
                    },
                    "5": {
                        "name": "com.example.NestedTestsTest$Level2BarTest",
                        "path": "com/example/NestedTestsTest$Level2BarTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "5780C183"
                    },
                    "6": {
                        "name": "com.example.NestedTestsTest$Level2FooTest",
                        "path": "com/example/NestedTestsTest$Level2FooTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "3E35762B"
                    },
                    "7": {
                        "name": "com.example.NestedTestsTest$Level2FooTest$Level3Test",
                        "path": "com/example/NestedTestsTest$Level2FooTest$Level3Test.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "FA83D453"
                    }
                },
                "tests": [
                    {
                        "class": 4,
                        "tags": ["PASSED"],
                        "coveredClasses": [0,1,2,3,4,5,6,7]
                    },
                    {
                        "class": 5,
                        "tags": ["PASSED"],
                        "coveredClasses": [3,4,5]
                    },
                    {
                        "class": 6,
                        "tags": ["PASSED"],
                        "coveredClasses": [1,2,4,6,7]
                    },
                    {
                        "class": 7,
                        "tags": ["PASSED"],
                        "coveredClasses": [2,4,6,7]
                    }
                ]
            }
        """);
        assertEquals(SKIP, testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT)).prediction());
        assertEquals(SKIP, testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest$Level2BarTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT)).prediction());
        assertEquals(SKIP, testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest$Level2FooTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT)).prediction());
        assertEquals(SKIP, testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest$Level2FooTest$Level3Test"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT)).prediction());
    }

    @Test
    void testPredictChangeInClassCoveredByNestedTest() throws ClassNotFoundException {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "id": "F87679D196B903DFE24335295E13DEED",
                "classes": {
                    "0": {
                        "name": "com.example.ClassA",
                        "path": "com/example/ClassA.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "3041DC84"
                    },
                    "1": {
                        "name": "com.example.ClassB",
                        "path": "com/example/ClassB.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "37FA6DE7"
                    },
                    "2": {
                        "name": "com.example.ClassC",
                        "path": "com/example/ClassC.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "00000000"
                    },
                    "3": {
                        "name": "com.example.ClassD",
                        "path": "com/example/ClassD.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "41C10CEE"
                    },
                    "4": {
                        "name": "com.example.NestedTestsTest",
                        "path": "com/example/NestedTestsTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "7797EB55"
                    },
                    "5": {
                        "name": "com.example.NestedTestsTest$Level2BarTest",
                        "path": "com/example/NestedTestsTest$Level2BarTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "5780C183"
                    },
                    "6": {
                        "name": "com.example.NestedTestsTest$Level2FooTest",
                        "path": "com/example/NestedTestsTest$Level2FooTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "3E35762B"
                    },
                    "7": {
                        "name": "com.example.NestedTestsTest$Level2FooTest$Level3Test",
                        "path": "com/example/NestedTestsTest$Level2FooTest$Level3Test.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "FA83D453"
                    }
                },
                "tests": [
                    {
                        "class": 4,
                        "tags": ["PASSED"],
                        "coveredClasses": [0,1,2,3,4,5,6,7]
                    },
                    {
                        "class": 5,
                        "tags": ["PASSED"],
                        "coveredClasses": [3,4,5]
                    },
                    {
                        "class": 6,
                        "tags": ["PASSED"],
                        "coveredClasses": [1,2,4,6,7]
                    },
                    {
                        "class": 7,
                        "tags": ["PASSED"],
                        "coveredClasses": [2,4,6,7]
                    }
                ]
            }
        """);
        assertEquals(EXECUTE, testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT)).prediction());
        assertEquals(SKIP, testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest$Level2BarTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT)).prediction());
        assertEquals(EXECUTE, testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest$Level2FooTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT)).prediction());
        assertEquals(EXECUTE, testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest$Level2FooTest$Level3Test"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT)).prediction());
    }

    @Test
    void testPredictFailureInNestedClass() throws ClassNotFoundException {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "id": "F87679D196B903DFE24335295E13DEED",
                "classes": {
                    "0": {
                        "name": "com.example.ClassA",
                        "path": "com/example/ClassA.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "3041DC84"
                    },
                    "1": {
                        "name": "com.example.ClassB",
                        "path": "com/example/ClassB.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "37FA6DE7"
                    },
                    "2": {
                        "name": "com.example.ClassC",
                        "path": "com/example/ClassC.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "B17DF734"
                    },
                    "3": {
                        "name": "com.example.ClassD",
                        "path": "com/example/ClassD.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "41C10CEE"
                    },
                    "4": {
                        "name": "com.example.NestedTestsTest",
                        "path": "com/example/NestedTestsTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "7797EB55"
                    },
                    "5": {
                        "name": "com.example.NestedTestsTest$Level2BarTest",
                        "path": "com/example/NestedTestsTest$Level2BarTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "5780C183"
                    },
                    "6": {
                        "name": "com.example.NestedTestsTest$Level2FooTest",
                        "path": "com/example/NestedTestsTest$Level2FooTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "3E35762B"
                    },
                    "7": {
                        "name": "com.example.NestedTestsTest$Level2FooTest$Level3Test",
                        "path": "com/example/NestedTestsTest$Level2FooTest$Level3Test.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "FA83D453"
                    }
                },
                "tests": [
                    {
                        "class": 4,
                        "tags": ["PASSED"],
                        "coveredClasses": [0,1,2,3,4,5,6,7]
                    },
                    {
                        "class": 5,
                        "tags": ["PASSED"],
                        "coveredClasses": [3,4,5]
                    },
                    {
                        "class": 6,
                        "tags": ["FAILED"],
                        "coveredClasses": [1,2,4,6,7]
                    },
                    {
                        "class": 7,
                        "tags": ["PASSED"],
                        "coveredClasses": [2,4,6,7]
                    }
                ]
            }
        """);

        var prediction = testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(Reason.Category.COVERED_TEST_TAGGED_AS_FAILED, prediction.reason().category());
        assertEquals(Optional.of("covered test: com.example.NestedTestsTest$Level2FooTest"), prediction.reason().details());

        prediction = testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest$Level2BarTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(SKIP, prediction.prediction());

        prediction = testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest$Level2FooTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(Reason.Category.TEST_FAILED_PREVIOUSLY, prediction.reason().category());

        prediction = testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest$Level2FooTest$Level3Test"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(Reason.Category.COVERED_TEST_TAGGED_AS_FAILED, prediction.reason().category());
        assertEquals(Optional.of("covered test: com.example.NestedTestsTest$Level2FooTest"), prediction.reason().details());
    }

}