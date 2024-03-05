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
                        "result": "PASSED",
                        "coveredClasses": ["0", "1"]
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
                        "hash": "9U3+WYit7uiiNqA9jplN2A=="                       
                    },
                    "1": {
                        "name": "com.example.LeftPadderTest",
                        "path": "io/skippy/common/model/LeftPadderTest.class",
                        "outputFolder": "src/test/resources",
                        "hash": "<!!!!>sGLJTZJw4beE9m2Kg6chUg=="                        
                    }
                },
                "tests": [
                    {
                        "class": "1",
                        "result": "PASSED",
                        "coveredClasses": ["0", "1"]
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
                        "hash": "<!!!!>9U3+WYit7uiiNqA9jplN2A=="                       
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
                        "result": "PASSED",
                        "coveredClasses": ["0", "1"]
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
                        "coveredClasses": ["0", "1"]
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
                        "coveredClasses": ["0", "1"]
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
                        "result": "PASSED",
                        "coveredClasses": ["0", "1"]
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