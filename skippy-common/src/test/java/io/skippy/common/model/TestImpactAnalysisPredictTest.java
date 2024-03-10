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

package io.skippy.common.model;

import org.junit.jupiter.api.Test;


import static io.skippy.common.model.Prediction.EXECUTE;
import static io.skippy.common.model.Prediction.SKIP;
import static io.skippy.common.model.Reason.Category.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestImpactAnalysisPredictTest {

    @Test
    void testPredictNoChange() {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.LeftPadder",
                        "path": "io/skippy/common/model/LeftPadder.class",
                        "outputFolder": "src/test/resources",
                        "hash": "8E994DD8"
                    },
                    "1": {
                        "name": "com.example.LeftPadderTest",
                        "path": "io/skippy/common/model/LeftPadderTest.class",
                        "outputFolder": "src/test/resources",
                        "hash": "83A72152"
                    }
                },
                "tests": [
                    {
                        "class": "1",
                        "result": "PASSED",
                        "coveredClasses": ["0", "1"],
                        "execution": "00000000000000000000000000000000"
                    }
                ]
            }
        """);
        var predictionWithReason = testImpactAnalysis.predict("com.example.LeftPadderTest");
        assertEquals(SKIP, predictionWithReason.prediction());
        assertEquals(NO_CHANGE, predictionWithReason.reason().category());
    }

    @Test
    void testPredictUnknownTest() {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {
                },
                "tests": [
                ]
            }
        """);
        var predictionWithReason = testImpactAnalysis.predict("com.example.LeftPadderTest");
        assertEquals(EXECUTE, predictionWithReason.prediction());
        assertEquals(NO_DATA_FOUND_FOR_TEST, predictionWithReason.reason().category());
    }

    @Test
    void testPredictBytecodeChangeInTest() {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.LeftPadder",
                        "path": "io/skippy/common/model/LeftPadder.class",
                        "outputFolder": "src/test/resources",
                        "hash": "8E994DD8"
                    },
                    "1": {
                        "name": "com.example.LeftPadderTest",
                        "path": "io/skippy/common/model/LeftPadderTest.class",
                        "outputFolder": "src/test/resources",
                        "hash": "00000000"
                    }
                },
                "tests": [
                    {
                        "class": "1",
                        "result": "PASSED",
                        "coveredClasses": ["0", "1"],
                        "execution": "00000000000000000000000000000000"
                    }
                ]
            }
        """);
        var predictionWithReason = testImpactAnalysis.predict("com.example.LeftPadderTest");
        assertEquals(EXECUTE, predictionWithReason.prediction());
        assertEquals(BYTECODE_CHANGE_IN_TEST, predictionWithReason.reason().category());
    }

    @Test
    void testPredictBytecodeChangeInCoveredClass() {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.LeftPadder",
                        "path": "io/skippy/common/model/LeftPadder.class",
                        "outputFolder": "src/test/resources",
                        "hash": "00000000"
                    },
                    "1": {
                        "name": "com.example.LeftPadderTest",
                        "path": "io/skippy/common/model/LeftPadderTest.class",
                        "outputFolder": "src/test/resources",
                        "hash": "83A72152"
                    }
                },
                "tests": [
                    {
                        "class": "1",
                        "result": "PASSED",
                        "coveredClasses": ["0", "1"],
                        "execution": "00000000000000000000000000000000"
                    }
                ]                  
            }
        """);
        var predictionWithReason = testImpactAnalysis.predict("com.example.LeftPadderTest");
        assertEquals(EXECUTE, predictionWithReason.prediction());
        assertEquals(BYTECODE_CHANGE_IN_COVERED_CLASS, predictionWithReason.reason().category());
        assertEquals("com.example.LeftPadder", predictionWithReason.reason().details().get());
    }

    @Test
    void testPredictFailedTest() {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.LeftPadder",
                        "path": "io/skippy/common/model/LeftPadder.class",
                        "outputFolder": "src/test/resources",
                        "hash": "9U3+WYit7uiiNqA9jplN2A=="                       
                    },
                    "1": {
                        "name": "com.example.LeftPadderTest",
                        "path": "io/skippy/common/model/LeftPadderTest.class",
                        "outputFolder": "src/test/resources",
                        "hash": "sGLJTZJw4beE9m2Kg6chUg=="                        
                    }
                },
                "tests": [
                    {
                        "class": "1",
                        "result": "FAILED",
                        "coveredClasses": ["0", "1"],
                        "execution": "00000000000000000000000000000000"                        
                    }
                ]                      
            }
        """);
        var predictionWithReason = testImpactAnalysis.predict("com.example.LeftPadderTest");
        assertEquals(EXECUTE, predictionWithReason.prediction());
        assertEquals(TEST_FAILED_PREVIOUSLY, predictionWithReason.reason().category());
    }

    @Test
    void testPredictTestClassFileNotFound() {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.LeftPadder",
                        "path": "io/skippy/common/model/LeftPadder.class",
                        "outputFolder": "src/test/resources",
                        "hash": "9U3+WYit7uiiNqA9jplN2A=="
                    },
                    "1": {
                        "name": "com.example.LeftPadderTest",
                        "path": "io/skippy/common/model/LeftPadderTest$Bla.class",
                        "outputFolder": "src/test/resources",
                        "hash": "sGLJTZJw4beE9m2Kg6chUg=="
                    }
                },
                "tests": [
                    {
                        "class": "1",
                        "result": "PASSED",
                        "coveredClasses": ["0", "1"],
                        "execution": "00000000000000000000000000000000"
                    }
                ]
            }
        """);
        var predictionWithReason = testImpactAnalysis.predict("com.example.LeftPadderTest");
        assertEquals(EXECUTE, predictionWithReason.prediction());
        assertEquals(TEST_CLASS_CLASS_FILE_NOT_FOUND, predictionWithReason.reason().category());
        assertEquals("io/skippy/common/model/LeftPadderTest$Bla.class", predictionWithReason.reason().details().get());
    }

    @Test
    void testPredictCoveredClassClassFileNotFound() {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
             {
                "classes": {
                    "0": {
                        "name": "com.example.LeftPadder",
                        "path": "io/skippy/common/model/LeftPadder$Bla.class",
                        "outputFolder": "src/test/resources",
                        "hash": "00000000"
                    },
                    "1": {
                        "name": "com.example.LeftPadderTest",
                        "path": "io/skippy/common/model/LeftPadderTest.class",
                        "outputFolder": "src/test/resources",
                        "hash": "83A72152"
                    }
                },
                "tests": [
                    {
                        "class": "1",
                        "result": "PASSED",
                        "coveredClasses": ["0", "1"],
                        "execution": "00000000000000000000000000000000"
                    }
                ]
            }
        """);
        var predictionWithReason = testImpactAnalysis.predict("com.example.LeftPadderTest");
        assertEquals(EXECUTE, predictionWithReason.prediction());
        assertEquals(COVERED_CLASS_CLASS_FILE_NOT_FOUND, predictionWithReason.reason().category());
        assertEquals("io/skippy/common/model/LeftPadder$Bla.class", predictionWithReason.reason().details().get());
    }

}