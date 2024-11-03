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
import static io.skippy.core.Reason.Category.TEST_TAGGED_AS_ALWAYS_EXECUTE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestImpactAnalysisPredictAlwaysExecuteTagTest {

    @Test
    void testTestTaggedAsAlwaysExecute() {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.LeftPadder",
                        "path": "io/skippy/core/LeftPadder.class",
                        "outputFolder": "src/test/resources",
                        "hash": "8E994DD8"
                    },
                    "1": {
                        "name": "com.example.LeftPadderTest",
                        "path": "io/skippy/core/LeftPadderTest.class",
                        "outputFolder": "src/test/resources",
                        "hash": "83A72152"
                    }
                },
                "tests": [
                    {
                        "class": "1",
                        "tags": ["PASSED", "ALWAYS_EXECUTE"],
                        "coveredClasses": ["0", "1"]
                    }
                ]
            }
        """);
        var predictionWithReason = testImpactAnalysis.predict("com.example.LeftPadderTest", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, predictionWithReason.prediction());
        assertEquals(TEST_TAGGED_AS_ALWAYS_EXECUTE, predictionWithReason.reason().category());
    }


    @Test
    public void testTestTaggedAsAlwaysExecuteScenario1() {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
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
                        "tags": ["PASSED", "ALWAYS_EXECUTE"],
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

        var prediction = testImpactAnalysis.predict("com.example.NestedTestsTest", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(TEST_TAGGED_AS_ALWAYS_EXECUTE, prediction.reason().category());

        prediction = testImpactAnalysis.predict("com.example.NestedTestsTest$Level2BarTest", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(Reason.Category.COVERED_TEST_TAGGED_AS_ALWAYS_EXECUTE, prediction.reason().category());
        assertEquals(Optional.of("covered test: com.example.NestedTestsTest"), prediction.reason().details());

        prediction = testImpactAnalysis.predict("com.example.NestedTestsTest$Level2FooTest", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(Reason.Category.COVERED_TEST_TAGGED_AS_ALWAYS_EXECUTE, prediction.reason().category());
        assertEquals(Optional.of("covered test: com.example.NestedTestsTest"), prediction.reason().details());

        prediction = testImpactAnalysis.predict("com.example.NestedTestsTest$Level2FooTest$Level3Test", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(Reason.Category.COVERED_TEST_TAGGED_AS_ALWAYS_EXECUTE, prediction.reason().category());
        assertEquals(Optional.of("covered test: com.example.NestedTestsTest"), prediction.reason().details());
    }

    @Test
    public void testTestTaggedAsAlwaysExecuteScenario2() {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
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
                        "tags": ["PASSED", "ALWAYS_EXECUTE"],
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

        var prediction = testImpactAnalysis.predict("com.example.NestedTestsTest", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(Reason.Category.COVERED_TEST_TAGGED_AS_ALWAYS_EXECUTE, prediction.reason().category());
        assertEquals(Optional.of("covered test: com.example.NestedTestsTest$Level2BarTest"), prediction.reason().details());

        prediction = testImpactAnalysis.predict("com.example.NestedTestsTest$Level2BarTest", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(TEST_TAGGED_AS_ALWAYS_EXECUTE, prediction.reason().category());

        prediction = testImpactAnalysis.predict("com.example.NestedTestsTest$Level2FooTest", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(SKIP, prediction.prediction());

        prediction = testImpactAnalysis.predict("com.example.NestedTestsTest$Level2FooTest$Level3Test", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(SKIP, prediction.prediction());
    }

    @Test
    public void testTestTaggedAsAlwaysExecuteScenario3() {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
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
                        "tags": ["PASSED", "ALWAYS_EXECUTE"],
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
        assertEquals(Reason.Category.COVERED_TEST_TAGGED_AS_ALWAYS_EXECUTE, prediction.reason().category());
        assertEquals(Optional.of("covered test: com.example.NestedTestsTest$Level2FooTest"), prediction.reason().details());

        prediction = testImpactAnalysis.predict("com.example.NestedTestsTest$Level2BarTest", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(SKIP, prediction.prediction());

        prediction = testImpactAnalysis.predict("com.example.NestedTestsTest$Level2FooTest", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(TEST_TAGGED_AS_ALWAYS_EXECUTE, prediction.reason().category());

        prediction = testImpactAnalysis.predict("com.example.NestedTestsTest$Level2FooTest$Level3Test", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(Reason.Category.COVERED_TEST_TAGGED_AS_ALWAYS_EXECUTE, prediction.reason().category());
        assertEquals(Optional.of("covered test: com.example.NestedTestsTest$Level2FooTest"), prediction.reason().details());
    }

    @Test
    public void testTestTaggedAsAlwaysExecuteScenario4() {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
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
                        "tags": ["PASSED", "ALWAYS_EXECUTE"],
                        "coveredClasses": [2,4,6,7]
                    }
                ]
            }
        """);

        var prediction = testImpactAnalysis.predict("com.example.NestedTestsTest", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(Reason.Category.COVERED_TEST_TAGGED_AS_ALWAYS_EXECUTE, prediction.reason().category());
        assertEquals(Optional.of("covered test: com.example.NestedTestsTest$Level2FooTest$Level3Test"), prediction.reason().details());

        prediction = testImpactAnalysis.predict("com.example.NestedTestsTest$Level2BarTest", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(SKIP, prediction.prediction());

        prediction = testImpactAnalysis.predict("com.example.NestedTestsTest$Level2FooTest", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(Reason.Category.COVERED_TEST_TAGGED_AS_ALWAYS_EXECUTE, prediction.reason().category());
        assertEquals(Optional.of("covered test: com.example.NestedTestsTest$Level2FooTest$Level3Test"), prediction.reason().details());

        prediction = testImpactAnalysis.predict("com.example.NestedTestsTest$Level2FooTest$Level3Test", SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(Reason.Category.TEST_TAGGED_AS_ALWAYS_EXECUTE, prediction.reason().category());
    }

}