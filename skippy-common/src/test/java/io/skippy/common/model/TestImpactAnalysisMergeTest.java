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
                        "result": "PASSED",
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
                        "result": "PASSED",
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
                        "result": "PASSED",
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
                        "result": "PASSED",
                        "coveredClasses": ["0"]
                    },
                    {
                        "class": "1",
                        "result": "PASSED",
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
                        "result": "FAILED",
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
                        "result": "FAILED",
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
                        "result": "FAILED",
                        "coveredClasses": ["0"]
                    }
                ]
            }
        """);
    }

    @Test
    void testMergeWithExecutionId() {
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
                        "result": "PASSED",
                        "coveredClasses": ["0"],
                        "executionId": "00000000000000000000000000000000"
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
                        "result": "PASSED",
                        "coveredClasses": ["0"],
                        "executionId": "11111111111111111111111111111111"
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
                        "result": "PASSED",
                        "coveredClasses": ["0"],
                        "executionId": "11111111111111111111111111111111"
                    }
                ]
            }
        """);
    }

}