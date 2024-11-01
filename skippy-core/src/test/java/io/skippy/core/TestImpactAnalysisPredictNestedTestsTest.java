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
    void testPredictNoChange() {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "id": "F87679D196B903DFE24335295E13DEED",
                "classes": {
                    "0": {
                        "name": "com.example.ClassA",
                        "path": "io/skippy/core/nested/ClassA.class",
                        "outputFolder": "src/test/resources",
                        "hash": "3041DC84"
                    },
                    "1": {
                        "name": "com.example.ClassB",
                        "path": "io/skippy/core/nested/ClassB.class",
                        "outputFolder": "src/test/resources",
                        "hash": "37FA6DE7"
                    },
                    "2": {
                        "name": "com.example.ClassC",
                        "path": "io/skippy/core/nested/ClassC.class",
                        "outputFolder": "src/test/resources",
                        "hash": "B17DF734"
                    },
                    "3": {
                        "name": "com.example.ClassD",
                        "path": "io/skippy/core/nested/ClassD.class",
                        "outputFolder": "src/test/resources",
                        "hash": "41C10CEE"
                    },
                    "4": {
                        "name": "com.example.NestedTestsTest",
                        "path": "io/skippy/core/nested/NestedTestsTest.class",
                        "outputFolder": "src/test/resources",
                        "hash": "C3A3737F"
                    },
                    "5": {
                        "name": "com.example.NestedTestsTest$Level2BarTest",
                        "path": "io/skippy/core/nested/NestedTestsTest$Level2BarTest.class",
                        "outputFolder": "src/test/resources",
                        "hash": "5780C183"
                    },
                    "6": {
                        "name": "com.example.NestedTestsTest$Level2FooTest",
                        "path": "io/skippy/core/nested/NestedTestsTest$Level2FooTest.class",
                        "outputFolder": "src/test/resources",
                        "hash": "3E35762B"
                    },
                    "7": {
                        "name": "com.example.NestedTestsTest$Level2FooTest$Level3Test",
                        "path": "io/skippy/core/nested/NestedTestsTest$Level2FooTest$Level3Test.class",
                        "outputFolder": "src/test/resources",
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
        assertEquals(SKIP, testImpactAnalysis.predict("com.example.NestedTestsTest", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT)).prediction());
        assertEquals(SKIP, testImpactAnalysis.predict("com.example.NestedTestsTest$Level2BarTest", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT)).prediction());
        assertEquals(SKIP, testImpactAnalysis.predict("com.example.NestedTestsTest$Level2FooTest", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT)).prediction());
        assertEquals(SKIP, testImpactAnalysis.predict("com.example.NestedTestsTest$Level2FooTest$Level3Test", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT)).prediction());
    }

    @Test
    void testPredictChangeInClassCoveredByNestedTest() {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "id": "F87679D196B903DFE24335295E13DEED",
                "classes": {
                    "0": {
                        "name": "com.example.ClassA",
                        "path": "io/skippy/core/nested/ClassA.class",
                        "outputFolder": "src/test/resources",
                        "hash": "3041DC84"
                    },
                    "1": {
                        "name": "com.example.ClassB",
                        "path": "io/skippy/core/nested/ClassB.class",
                        "outputFolder": "src/test/resources",
                        "hash": "37FA6DE7"
                    },
                    "2": {
                        "name": "com.example.ClassC",
                        "path": "io/skippy/core/nested/ClassC.class",
                        "outputFolder": "src/test/resources",
                        "hash": "00000000"
                    },
                    "3": {
                        "name": "com.example.ClassD",
                        "path": "io/skippy/core/nested/ClassD.class",
                        "outputFolder": "src/test/resources",
                        "hash": "41C10CEE"
                    },
                    "4": {
                        "name": "com.example.NestedTestsTest",
                        "path": "io/skippy/core/nested/NestedTestsTest.class",
                        "outputFolder": "src/test/resources",
                        "hash": "C3A3737F"
                    },
                    "5": {
                        "name": "com.example.NestedTestsTest$Level2BarTest",
                        "path": "io/skippy/core/nested/NestedTestsTest$Level2BarTest.class",
                        "outputFolder": "src/test/resources",
                        "hash": "5780C183"
                    },
                    "6": {
                        "name": "com.example.NestedTestsTest$Level2FooTest",
                        "path": "io/skippy/core/nested/NestedTestsTest$Level2FooTest.class",
                        "outputFolder": "src/test/resources",
                        "hash": "3E35762B"
                    },
                    "7": {
                        "name": "com.example.NestedTestsTest$Level2FooTest$Level3Test",
                        "path": "io/skippy/core/nested/NestedTestsTest$Level2FooTest$Level3Test.class",
                        "outputFolder": "src/test/resources",
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
        assertEquals(EXECUTE, testImpactAnalysis.predict("com.example.NestedTestsTest", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT)).prediction());
        assertEquals(SKIP, testImpactAnalysis.predict("com.example.NestedTestsTest$Level2BarTest", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT)).prediction());
        assertEquals(EXECUTE, testImpactAnalysis.predict("com.example.NestedTestsTest$Level2FooTest", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT)).prediction());
        assertEquals(EXECUTE, testImpactAnalysis.predict("com.example.NestedTestsTest$Level2FooTest$Level3Test", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT)).prediction());
    }

    @Test
    void testPredictFailureInNestedClass() {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "id": "F87679D196B903DFE24335295E13DEED",
                "classes": {
                    "0": {
                        "name": "com.example.ClassA",
                        "path": "io/skippy/core/nested/ClassA.class",
                        "outputFolder": "src/test/resources",
                        "hash": "3041DC84"
                    },
                    "1": {
                        "name": "com.example.ClassB",
                        "path": "io/skippy/core/nested/ClassB.class",
                        "outputFolder": "src/test/resources",
                        "hash": "37FA6DE7"
                    },
                    "2": {
                        "name": "com.example.ClassC",
                        "path": "io/skippy/core/nested/ClassC.class",
                        "outputFolder": "src/test/resources",
                        "hash": "B17DF734"
                    },
                    "3": {
                        "name": "com.example.ClassD",
                        "path": "io/skippy/core/nested/ClassD.class",
                        "outputFolder": "src/test/resources",
                        "hash": "41C10CEE"
                    },
                    "4": {
                        "name": "com.example.NestedTestsTest",
                        "path": "io/skippy/core/nested/NestedTestsTest.class",
                        "outputFolder": "src/test/resources",
                        "hash": "C3A3737F"
                    },
                    "5": {
                        "name": "com.example.NestedTestsTest$Level2BarTest",
                        "path": "io/skippy/core/nested/NestedTestsTest$Level2BarTest.class",
                        "outputFolder": "src/test/resources",
                        "hash": "5780C183"
                    },
                    "6": {
                        "name": "com.example.NestedTestsTest$Level2FooTest",
                        "path": "io/skippy/core/nested/NestedTestsTest$Level2FooTest.class",
                        "outputFolder": "src/test/resources",
                        "hash": "3E35762B"
                    },
                    "7": {
                        "name": "com.example.NestedTestsTest$Level2FooTest$Level3Test",
                        "path": "io/skippy/core/nested/NestedTestsTest$Level2FooTest$Level3Test.class",
                        "outputFolder": "src/test/resources",
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

        var prediction = testImpactAnalysis.predict("com.example.NestedTestsTest", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(Reason.Category.COVERED_TEST_FAILED_PREVIOUSLY, prediction.reason().category());
        assertEquals(Optional.of("com.example.NestedTestsTest$Level2FooTest"), prediction.reason().details());

        prediction = testImpactAnalysis.predict("com.example.NestedTestsTest$Level2BarTest", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(SKIP, prediction.prediction());

        prediction = testImpactAnalysis.predict("com.example.NestedTestsTest$Level2FooTest", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(Reason.Category.TEST_FAILED_PREVIOUSLY, prediction.reason().category());

        prediction = testImpactAnalysis.predict("com.example.NestedTestsTest$Level2FooTest$Level3Test", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(Reason.Category.COVERED_TEST_FAILED_PREVIOUSLY, prediction.reason().category());
        assertEquals(Optional.of("com.example.NestedTestsTest$Level2FooTest"), prediction.reason().details());
    }

}