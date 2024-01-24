package io.skippy.common.model;

import org.junit.jupiter.api.Test;


import static io.skippy.common.model.Prediction.EXECUTE;
import static io.skippy.common.model.Prediction.SKIP;
import static io.skippy.common.model.Reason.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestImpactAnalysisPredictTest {

    @Test
    void testPredictNoChange() {
        var testImpactAnalysis = TestImpactAnalysis.parse(
            """
                [
                    {
                        "testClass": {
                            "class": "com.example.LeftPadderTest",
                            "path": "io/skippy/common/model/LeftPadderTest.class",
                            "outputFolder": "src/test/resources",
                            "hash": "sGLJTZJw4beE9m2Kg6chUg=="
                        },
                        "result": "SUCCESS",
                        "coveredClasses": [
                            {
                                "class": "com.example.LeftPadder",
                                "path": "io/skippy/common/model/LeftPadder.class",
                                "outputFolder": "src/test/resources",
                                "hash": "9U3+WYit7uiiNqA9jplN2A=="
                            }
                        ]
                    }
                ]
            """);
        var predictionWithReason = testImpactAnalysis.predict("com.example.LeftPadderTest");
        assertEquals(SKIP, predictionWithReason.prediction());
        assertEquals(NO_CHANGE, predictionWithReason.reason());
    }

    @Test
    void testPredictUnknownTest() {
        var testImpactAnalysis = TestImpactAnalysis.parse(
                """
                    []
                """);
        var predictionWithReason = testImpactAnalysis.predict("com.example.LeftPadderTest");
        assertEquals(EXECUTE, predictionWithReason.prediction());
        assertEquals(UNKNOWN_TEST, predictionWithReason.reason());
    }

    @Test
    void testPredictBytecodeChangeInTest() {
        var testImpactAnalysis = TestImpactAnalysis.parse(
                """
                    [
                        {
                            "testClass": {
                                "class": "com.example.LeftPadderTest",
                                "path": "io/skippy/common/model/LeftPadderTest.class",
                                "outputFolder": "src/test/resources",
                                "hash": "<!!!!>sGLJTZJw4beE9m2Kg6chUg=="
                            },
                            "result": "SUCCESS",
                            "coveredClasses": [
                                {
                                    "class": "com.example.LeftPadder",
                                    "path": "io/skippy/common/model/LeftPadder.class",
                                    "outputFolder": "src/test/resources",
                                    "hash": "9U3+WYit7uiiNqA9jplN2A=="
                                }
                            ]
                        }
                    ]
                """);
        var predictionWithReason = testImpactAnalysis.predict("com.example.LeftPadderTest");
        assertEquals(EXECUTE, predictionWithReason.prediction());
        assertEquals(BYTECODE_CHANGE_IN_TEST, predictionWithReason.reason());
    }

    @Test
    void testPredictBytecodeChangeInCoveredClass() {
        var testImpactAnalysis = TestImpactAnalysis.parse(
                """
                    [
                        {
                            "testClass": {
                                "class": "com.example.LeftPadderTest",
                                "path": "io/skippy/common/model/LeftPadderTest.class",
                                "outputFolder": "src/test/resources",
                                "hash": "sGLJTZJw4beE9m2Kg6chUg=="
                            },
                            "result": "SUCCESS",
                            "coveredClasses": [
                                {
                                    "class": "com.example.LeftPadder",
                                    "path": "io/skippy/common/model/LeftPadder.class",
                                    "outputFolder": "src/test/resources",
                                    "hash": "<!!!!>9U3+WYit7uiiNqA9jplN2A=="
                                }
                            ]
                        }
                    ]
                """);
        var predictionWithReason = testImpactAnalysis.predict("com.example.LeftPadderTest");
        assertEquals(EXECUTE, predictionWithReason.prediction());
        assertEquals(BYTECODE_CHANGE_IN_COVERED_CLASS, predictionWithReason.reason());
    }

}
