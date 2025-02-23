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

import com.example.NestedTestsTest;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Optional;

import static io.skippy.core.Prediction.EXECUTE;
import static io.skippy.core.Prediction.SKIP;
import static io.skippy.core.Reason.Category.TEST_TAGGED_AS_ALWAYS_EXECUTE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestImpactAnalysisPredictAlwaysExecuteTagTest {

    @Test
    void testTestTaggedAsAlwaysExecute() throws ClassNotFoundException {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.LeftPadder",
                        "path": "com/example/LeftPadder.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "8E994DD8"
                    },
                    "1": {
                        "name": "com.example.LeftPadderTest",
                        "path": "com/example/LeftPadderTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "80E52EBA"
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
        var predictionWithReason = testImpactAnalysis.predict(Class.forName("com.example.LeftPadderTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, predictionWithReason.prediction());
        assertEquals(TEST_TAGGED_AS_ALWAYS_EXECUTE, predictionWithReason.reason().category());
    }


    @Test
    public void testTestTaggedAsAlwaysExecuteScenario1() throws ClassNotFoundException {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.ClassA",
                        "path": "com/example/ClassA.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "0DDD7384"
                    },
                    "1": {
                        "name": "com.example.ClassB",
                        "path": "com/example/ClassB.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "D8D9EC2D"
                    },
                    "2": {
                        "name": "com.example.ClassC",
                        "path": "com/example/ClassC.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "61B23117"
                    },
                    "3": {
                        "name": "com.example.ClassD",
                        "path": "com/example/ClassD.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "280B53B2"
                    },
                    "4": {
                        "name": "com.example.NestedTestsTest",
                        "path": "com/example/NestedTestsTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "492D2AD3"
                    },
                    "5": {
                        "name": "com.example.NestedTestsTest$Level2BarTest",
                        "path": "com/example/NestedTestsTest$Level2BarTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "30316E0B"
                    },
                    "6": {
                        "name": "com.example.NestedTestsTest$Level2FooTest",
                        "path": "com/example/NestedTestsTest$Level2FooTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "D63C7B06"
                    },
                    "7": {
                        "name": "com.example.NestedTestsTest$Level2FooTest$Level3Test",
                        "path": "com/example/NestedTestsTest$Level2FooTest$Level3Test.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "9AFD1143"
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

        var prediction = testImpactAnalysis.predict(NestedTestsTest.class, SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(TEST_TAGGED_AS_ALWAYS_EXECUTE, prediction.reason().category());

        prediction = testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest$Level2BarTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(Reason.Category.COVERED_TEST_TAGGED_AS_ALWAYS_EXECUTE, prediction.reason().category());
        assertEquals(Optional.of("covered test: com.example.NestedTestsTest"), prediction.reason().details());

        prediction = testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest$Level2FooTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(Reason.Category.COVERED_TEST_TAGGED_AS_ALWAYS_EXECUTE, prediction.reason().category());
        assertEquals(Optional.of("covered test: com.example.NestedTestsTest"), prediction.reason().details());

        prediction = testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest$Level2FooTest$Level3Test"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(Reason.Category.COVERED_TEST_TAGGED_AS_ALWAYS_EXECUTE, prediction.reason().category());
        assertEquals(Optional.of("covered test: com.example.NestedTestsTest"), prediction.reason().details());
    }

    private Class<?> loadClass(String outputFolder, String className) {
        try {
            URL outputFolderUrl = getClass().getClassLoader().getResource(outputFolder);
            URLClassLoader classLoader = new URLClassLoader(new URL[]{ outputFolderUrl });
            return classLoader.loadClass(className);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testTestTaggedAsAlwaysExecuteScenario2() throws ClassNotFoundException {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.ClassA",
                        "path": "com/example/ClassA.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "0DDD7384"
                    },
                    "1": {
                        "name": "com.example.ClassB",
                        "path": "com/example/ClassB.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "D8D9EC2D"
                    },
                    "2": {
                        "name": "com.example.ClassC",
                        "path": "com/example/ClassC.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "61B23117"
                    },
                    "3": {
                        "name": "com.example.ClassD",
                        "path": "com/example/ClassD.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "280B53B2"
                    },
                    "4": {
                        "name": "com.example.NestedTestsTest",
                        "path": "com/example/NestedTestsTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "492D2AD3"
                    },
                    "5": {
                        "name": "com.example.NestedTestsTest$Level2BarTest",
                        "path": "com/example/NestedTestsTest$Level2BarTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "30316E0B"
                    },
                    "6": {
                        "name": "com.example.NestedTestsTest$Level2FooTest",
                        "path": "com/example/NestedTestsTest$Level2FooTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "D63C7B06"
                    },
                    "7": {
                        "name": "com.example.NestedTestsTest$Level2FooTest$Level3Test",
                        "path": "com/example/NestedTestsTest$Level2FooTest$Level3Test.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "9AFD1143"
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

        var prediction = testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(Reason.Category.COVERED_TEST_TAGGED_AS_ALWAYS_EXECUTE, prediction.reason().category());
        assertEquals(Optional.of("covered test: com.example.NestedTestsTest$Level2BarTest"), prediction.reason().details());

        prediction = testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest$Level2BarTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(TEST_TAGGED_AS_ALWAYS_EXECUTE, prediction.reason().category());

        prediction = testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest$Level2FooTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(SKIP, prediction.prediction());

        prediction = testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest$Level2FooTest$Level3Test"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(SKIP, prediction.prediction());
    }

    @Test
    public void testTestTaggedAsAlwaysExecuteScenario3() throws ClassNotFoundException {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.ClassA",
                        "path": "com/example/ClassA.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "0DDD7384"
                    },
                    "1": {
                        "name": "com.example.ClassB",
                        "path": "com/example/ClassB.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "D8D9EC2D"
                    },
                    "2": {
                        "name": "com.example.ClassC",
                        "path": "com/example/ClassC.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "61B23117"
                    },
                    "3": {
                        "name": "com.example.ClassD",
                        "path": "com/example/ClassD.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "280B53B2"
                    },
                    "4": {
                        "name": "com.example.NestedTestsTest",
                        "path": "com/example/NestedTestsTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "492D2AD3"
                    },
                    "5": {
                        "name": "com.example.NestedTestsTest$Level2BarTest",
                        "path": "com/example/NestedTestsTest$Level2BarTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "30316E0B"
                    },
                    "6": {
                        "name": "com.example.NestedTestsTest$Level2FooTest",
                        "path": "com/example/NestedTestsTest$Level2FooTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "D63C7B06"
                    },
                    "7": {
                        "name": "com.example.NestedTestsTest$Level2FooTest$Level3Test",
                        "path": "com/example/NestedTestsTest$Level2FooTest$Level3Test.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "9AFD1143"
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

        var prediction = testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(Reason.Category.COVERED_TEST_TAGGED_AS_ALWAYS_EXECUTE, prediction.reason().category());
        assertEquals(Optional.of("covered test: com.example.NestedTestsTest$Level2FooTest"), prediction.reason().details());

        prediction = testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest$Level2BarTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(SKIP, prediction.prediction());

        prediction = testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest$Level2FooTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(TEST_TAGGED_AS_ALWAYS_EXECUTE, prediction.reason().category());

        prediction = testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest$Level2FooTest$Level3Test"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(Reason.Category.COVERED_TEST_TAGGED_AS_ALWAYS_EXECUTE, prediction.reason().category());
        assertEquals(Optional.of("covered test: com.example.NestedTestsTest$Level2FooTest"), prediction.reason().details());
    }

    @Test
    public void testTestTaggedAsAlwaysExecuteScenario4() throws ClassNotFoundException {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.ClassA",
                        "path": "com/example/ClassA.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "0DDD7384"
                    },
                    "1": {
                        "name": "com.example.ClassB",
                        "path": "com/example/ClassB.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "D8D9EC2D"
                    },
                    "2": {
                        "name": "com.example.ClassC",
                        "path": "com/example/ClassC.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "61B23117"
                    },
                    "3": {
                        "name": "com.example.ClassD",
                        "path": "com/example/ClassD.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "280B53B2"
                    },
                    "4": {
                        "name": "com.example.NestedTestsTest",
                        "path": "com/example/NestedTestsTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "492D2AD3"
                    },
                    "5": {
                        "name": "com.example.NestedTestsTest$Level2BarTest",
                        "path": "com/example/NestedTestsTest$Level2BarTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "30316E0B"
                    },
                    "6": {
                        "name": "com.example.NestedTestsTest$Level2FooTest",
                        "path": "com/example/NestedTestsTest$Level2FooTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "D63C7B06"
                    },
                    "7": {
                        "name": "com.example.NestedTestsTest$Level2FooTest$Level3Test",
                        "path": "com/example/NestedTestsTest$Level2FooTest$Level3Test.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "9AFD1143"
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

        var prediction = testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(Reason.Category.COVERED_TEST_TAGGED_AS_ALWAYS_EXECUTE, prediction.reason().category());
        assertEquals(Optional.of("covered test: com.example.NestedTestsTest$Level2FooTest$Level3Test"), prediction.reason().details());

        prediction = testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest$Level2BarTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(SKIP, prediction.prediction());

        prediction = testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest$Level2FooTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(Reason.Category.COVERED_TEST_TAGGED_AS_ALWAYS_EXECUTE, prediction.reason().category());
        assertEquals(Optional.of("covered test: com.example.NestedTestsTest$Level2FooTest$Level3Test"), prediction.reason().details());

        prediction = testImpactAnalysis.predict(Class.forName("com.example.NestedTestsTest$Level2FooTest$Level3Test"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
        assertEquals(EXECUTE, prediction.prediction());
        assertEquals(Reason.Category.TEST_TAGGED_AS_ALWAYS_EXECUTE, prediction.reason().category());
    }

}