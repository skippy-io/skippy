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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


import java.util.Optional;

import static io.skippy.core.Prediction.EXECUTE;
import static io.skippy.core.Prediction.SKIP;
import static io.skippy.core.Reason.Category.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestImpactAnalysisPredictTest {

    @Nested
    class ScenariosWithCoverageForSkippedTestsDisabled {

        @Test
        void testNoTestImpactAnalysisFound() throws ClassNotFoundException {
            var testImpactAnalysis = TestImpactAnalysis.NOT_FOUND;
            var predictionWithReason = testImpactAnalysis.predict(Class.forName("com.example.LeftPadderTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
            assertEquals(EXECUTE, predictionWithReason.prediction());
            assertEquals(TEST_IMPACT_ANALYSIS_NOT_FOUND, predictionWithReason.reason().category());
        }

        @Test
        void testPredictNoChange() throws ClassNotFoundException {
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
                        "tags": ["PASSED"],
                        "coveredClasses": ["0", "1"]
                    }
                ]
            }
        """);
            var predictionWithReason = testImpactAnalysis.predict(Class.forName("com.example.LeftPadderTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
            assertEquals(SKIP, predictionWithReason.prediction());
            assertEquals(NO_CHANGE, predictionWithReason.reason().category());
        }

        @Test
        void testPredictUnknownTest() throws ClassNotFoundException {
            var testImpactAnalysis = TestImpactAnalysis.parse("""
                {
                    "classes": {
                        "0": {
                            "name": "com.example.LeftPadder",
                            "path": "com/example/LeftPadder.class",
                            "outputFolder": "build/classes/java/test",
                            "hash": "8E994DD8"
                        }
                    },
                    "tests": [
                    ]
                }
            """);
            var predictionWithReason = testImpactAnalysis.predict(Class.forName("com.example.LeftPadderTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
            assertEquals(EXECUTE, predictionWithReason.prediction());
            assertEquals(NO_DATA_FOUND_FOR_TEST, predictionWithReason.reason().category());
        }

        @Test
        void testPredictBytecodeChangeInTest() throws ClassNotFoundException {
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
                        "hash": "00000000"
                    }
                },
                "tests": [
                    {
                        "class": "1",
                        "tags": ["PASSED"],
                        "coveredClasses": ["0", "1"]
                    }
                ]
            }
        """);
            var predictionWithReason = testImpactAnalysis.predict(Class.forName("com.example.LeftPadderTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
            assertEquals(EXECUTE, predictionWithReason.prediction());
            assertEquals(BYTECODE_CHANGE_IN_TEST, predictionWithReason.reason().category());
        }

        @Test
        void testPredictBytecodeChangeInCoveredClass() throws ClassNotFoundException {
            var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.LeftPadder",
                        "path": "com/example/LeftPadder.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "00000000"
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
                        "tags": ["PASSED"],
                        "coveredClasses": ["0", "1"]
                    }
                ]
            }
        """);
            var predictionWithReason = testImpactAnalysis.predict(Class.forName("com.example.LeftPadderTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
            assertEquals(EXECUTE, predictionWithReason.prediction());
            assertEquals(BYTECODE_CHANGE_IN_COVERED_CLASS, predictionWithReason.reason().category());
            assertEquals("covered class: com.example.LeftPadder", predictionWithReason.reason().details().get());
        }

        @Test
        void testPredictFailedTest() throws ClassNotFoundException {
            var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.LeftPadder",
                        "path": "com/example/LeftPadder.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "9U3+WYit7uiiNqA9jplN2A=="
                    },
                    "1": {
                        "name": "com.example.LeftPadderTest",
                        "path": "com/example/LeftPadderTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "sGLJTZJw4beE9m2Kg6chUg=="
                    }
                },
                "tests": [
                    {
                        "class": "1",
                        "tags": ["FAILED"],
                        "coveredClasses": ["0", "1"]
                    }
                ]                      
            }
        """);
            var predictionWithReason = testImpactAnalysis.predict(Class.forName("com.example.LeftPadderTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
            assertEquals(EXECUTE, predictionWithReason.prediction());
            assertEquals(TEST_FAILED_PREVIOUSLY, predictionWithReason.reason().category());
        }

        @Test
        void testPredictTestClassFileNotFound() throws ClassNotFoundException {
            var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.LeftPadder",
                        "path": "com/example/LeftPadder.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "9U3+WYit7uiiNqA9jplN2A=="
                    },
                    "1": {
                        "name": "com.example.LeftPadderTest",
                        "path": "com/example/LeftPadderTest$Bla.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "sGLJTZJw4beE9m2Kg6chUg=="
                    }
                },
                "tests": [
                    {
                        "class": "1",
                        "tags": ["PASSED"],
                        "coveredClasses": ["0", "1"]
                    }
                ]
            }
        """);
            var predictionWithReason = testImpactAnalysis.predict(Class.forName("com.example.LeftPadderTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
            assertEquals(EXECUTE, predictionWithReason.prediction());
            assertEquals(TEST_CLASS_CLASS_FILE_NOT_FOUND, predictionWithReason.reason().category());
            assertEquals("test class file: com/example/LeftPadderTest$Bla.class", predictionWithReason.reason().details().get());
        }

        @Test
        void testPredictCoveredClassClassFileNotFound() throws ClassNotFoundException {
            var testImpactAnalysis = TestImpactAnalysis.parse("""
             {
                "classes": {
                    "0": {
                        "name": "com.example.LeftPadder",
                        "path": "com/example/LeftPadder$Bla.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "00000000"
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
                        "tags": ["PASSED"],
                        "coveredClasses": ["0", "1"]
                    }
                ]
            }
        """);
            var predictionWithReason = testImpactAnalysis.predict(Class.forName("com.example.LeftPadderTest"), SkippyConfiguration.DEFAULT, SkippyRepository.getInstance(SkippyConfiguration.DEFAULT));
            assertEquals(EXECUTE, predictionWithReason.prediction());
            assertEquals(COVERED_CLASS_CLASS_FILE_NOT_FOUND, predictionWithReason.reason().category());
            assertEquals("covered class: com/example/LeftPadder$Bla.class", predictionWithReason.reason().details().get());
        }

    }

    @Nested
    class ScenariosWithCoverageForSkippedTestsEnabled {

        @Test
        void testHappyPath() throws ClassNotFoundException {
            var testImpactAnalysis = TestImpactAnalysis.parse("""
                {
                    "classes": {
                        "0": {
                            "name": "com.example.LeftPadderTest",
                            "path": "com/example/LeftPadderTest.class",
                            "outputFolder": "build/classes/java/test",
                            "hash": "80E52EBA"
                        }
                    },
                    "tests": [
                        {
                            "class": "0",
                            "tags": ["PASSED"],
                            "coveredClasses": ["0"],
                            "executionId": "00000000000000000000000000000000"
                        }
                    ]
                }
            """);
            var configuration = new SkippyConfiguration(true, Optional.empty(), Optional.empty());
            var repository = mock(SkippyRepository.class);
            when(repository.readJacocoExecutionData("00000000000000000000000000000000")).thenReturn(Optional.of(new byte[]{}));
            var predictionWithReason = testImpactAnalysis.predict(Class.forName("com.example.LeftPadderTest"), configuration, repository);
            assertEquals(SKIP, predictionWithReason.prediction());
        }


        @Test
        void testMissingExecutionId() throws ClassNotFoundException {
            var testImpactAnalysis = TestImpactAnalysis.parse("""
                {
                    "classes": {
                        "0": {
                            "name": "com.example.LeftPadderTest",
                            "path": "com/example/LeftPadderTest.class",
                            "outputFolder": "build/classes/java/test",
                            "hash": "80E52EBA"
                        }
                    },
                    "tests": [
                        {
                            "class": "0",
                            "tags": ["PASSED"],
                            "coveredClasses": ["0"]
                        }
                    ]
                }
            """);
            var configuration = new SkippyConfiguration(true, Optional.empty(), Optional.empty());
            var repository = SkippyRepository.getInstance(configuration);
            var predictionWithReason = testImpactAnalysis.predict(Class.forName("com.example.LeftPadderTest"), configuration, repository);
            assertEquals(EXECUTE, predictionWithReason.prediction());
            assertEquals(MISSING_EXECUTION_ID, predictionWithReason.reason().category());
        }

        @Test
        void testUnableToReadExecutionData() throws ClassNotFoundException {
            var testImpactAnalysis = TestImpactAnalysis.parse("""
                {
                    "classes": {
                        "0": {
                            "name": "com.example.LeftPadderTest",
                            "path": "com/example/LeftPadderTest.class",
                            "outputFolder": "build/classes/java/test",
                            "hash": "80E52EBA"
                        }
                    },
                    "tests": [
                        {
                            "class": "0",
                            "tags": ["PASSED"],
                            "coveredClasses": ["0"],
                            "executionId": "00000000000000000000000000000000"
                        }
                    ]
                }
            """);
            var configuration = new SkippyConfiguration(true, Optional.empty(), Optional.empty());
            var repository = SkippyRepository.getInstance(configuration);
            var predictionWithReason = testImpactAnalysis.predict(Class.forName("com.example.LeftPadderTest"), configuration, repository);
            assertEquals(EXECUTE, predictionWithReason.prediction());
            assertEquals(UNABLE_TO_READ_EXECUTION_DATA, predictionWithReason.reason().category());
        }

    }

}