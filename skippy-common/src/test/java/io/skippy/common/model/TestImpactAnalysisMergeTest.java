package io.skippy.common.model;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
                        "coveredClasses": ["0"]
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
                        "coveredClasses": ["0"]
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
                        "coveredClasses": ["0"]
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
                        "coveredClasses": ["0"]
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
                        "coveredClasses": ["0"]
                    },
                    {
                        "class": "1",
                        "coveredClasses": ["1"]
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
                        "coveredClasses": ["0"]
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
                        "coveredClasses": ["0"]
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
                        "coveredClasses": ["0"]
                    }
                ]
            }
        """);
    }

}