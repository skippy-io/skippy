package io.skippy.common.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestImpactAnalysisMergeTest {

    @Test
    void testEmptyBaseline() {
        var baseline = TestImpactAnalysis.parse("""
            {
                "classes": {
                },
                "tests": [
                ]
            }
        """);
        var newAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {
                },
                "tests": [
                ]
            }
        """);
        var mergedAnalysis = baseline.merge(newAnalysis);
        assertThat(mergedAnalysis.toJson()).isEqualToIgnoringWhitespace("""
            {
                "classes": {
                },
                "tests": [
                ]
            }
        """);
    }

    @Test
    void testEmptyBaselineNewClassAndNewTest() {
        var baseline = TestImpactAnalysis.parse("""
            {
                "classes": {
                },
                "tests": [
                ]
            }
        """);
        var newAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.FooTest",
                        "path": "com/example/FooTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "FooTest#hash"
                    }
                },
                "tests": [
                    {
                        "class": "0",
                        "result": "PASSED",
                        "coveredClasses": ["0"],
                        "jacocoId": "00000000000000000000000000000000"
                    }
                ]
            }
        """);
        var mergedAnalysis = baseline.merge(newAnalysis);
        assertThat(mergedAnalysis.toJson()).isEqualToIgnoringWhitespace("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.FooTest",
                        "path": "com/example/FooTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "FooTest#hash"
                    }
                },
                "tests": [
                    {
                        "class": "0",
                        "result": "PASSED",
                        "coveredClasses": ["0"],
                        "jacocoId": "00000000000000000000000000000000"
                    }
                ]
            }
        """);
    }

    @Test
    void testAdditionalClassAndTest() {
        var baseline = TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.FooTest",
                        "path": "com/example/FooTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "FooTest#hash"
                    }
                },
                "tests": [
                    {
                        "class": "0",
                        "result": "PASSED",
                        "coveredClasses": ["0"],
                        "jacocoId": "00000000000000000000000000000000"
                    }
                ]
            }
        """);
        var newAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.BarTest",
                        "path": "com/example/BarTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "BarTest#hash"
                    }
                },
                "tests": [
                    {
                        "class": "0",
                        "result": "PASSED",
                        "coveredClasses": ["0"],
                        "jacocoId": "11111111111111111111111111111111"
                    }
                ]
            }
        """);
        var mergedAnalysis = baseline.merge(newAnalysis);
        assertThat(mergedAnalysis.toJson()).isEqualToIgnoringWhitespace("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.BarTest",
                        "path": "com/example/BarTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "BarTest#hash"
                    },
                    "1": {
                        "name": "com.example.FooTest",
                        "path": "com/example/FooTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "FooTest#hash"
                    }
                },
                "tests": [
                    {
                        "class": "0",
                        "result": "PASSED",
                        "coveredClasses": ["0"],
                        "jacocoId": "11111111111111111111111111111111"
                    },
                    {
                        "class": "1",
                        "result": "PASSED",
                        "coveredClasses": ["1"],
                        "jacocoId": "00000000000000000000000000000000"
                    }
                ]
            }
        """);
    }

    @Test
    void testUpdatedClassAndTest() {
        var baseline = TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.FooTest",
                        "path": "com/example/FooTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "FooTest#hash-new"
                    }
                },
                "tests": [
                    {
                        "class": "0",
                        "result": "FAILED",
                        "coveredClasses": ["0"],
                        "jacocoId": "00000000000000000000000000000000"
                    }
                ]
            }
        """);
        var newAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.FooTest",
                        "path": "com/example/FooTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "FooTest#hash-new"
                    }
                },
                "tests": [
                    {
                        "class": "0",
                        "result": "FAILED",
                        "coveredClasses": ["0"],
                        "jacocoId": "11111111111111111111111111111111"
                    }
                ]
            }
        """);
        var mergedAnalysis = baseline.merge(newAnalysis);
        assertThat(mergedAnalysis.toJson()).isEqualToIgnoringWhitespace("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.FooTest",
                        "path": "com/example/FooTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "FooTest#hash-new"
                    }
                },
                "tests": [
                    {
                        "class": "0",
                        "result": "FAILED",
                        "coveredClasses": ["0"],
                        "jacocoId": "11111111111111111111111111111111"
                    }
                ]
            }
        """);
    }

}