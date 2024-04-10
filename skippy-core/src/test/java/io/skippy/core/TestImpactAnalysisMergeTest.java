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

import io.skippy.core.TestImpactAnalysis;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestImpactAnalysisMergeTest {

    @Test
    void testEmptyBaseline() {
        var baseline = TestImpactAnalysis.parse("""
            {
                "classes": {},
                "tests": []
            }
        """);
        var newAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {},
                "tests": []
            }
        """);
        var mergedAnalysis = baseline.merge(newAnalysis);
        assertThat(mergedAnalysis.toJson()).isEqualToIgnoringWhitespace("""
            {
                "id": "F8D85DB143EC3F06FAD5D0E0C730E1E9",
                "classes": {},
                "tests": []
            }
        """);
    }

    @Test
    void testEmptyBaselineNewClassAndNewTest() {
        var baseline = TestImpactAnalysis.parse("""
            {
                "classes": {},
                "tests": []
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
                "id": "2DF824AB81F8E6FB957D5E16DA86B981",
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
                        "class": 0,
                        "result": "PASSED",
                        "coveredClasses": [0]
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
                "id": "11354F8AC8619304F48F36FB5D4458C3",
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
                        "class": 0,
                        "result": "PASSED",
                        "coveredClasses": [0]
                    },
                    {
                        "class": 1,
                        "result": "PASSED",
                        "coveredClasses": [1]
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
                "id": "316A0F66B5E6E1C3993C60575ECA82FD",
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
                        "class": 0,
                        "result": "FAILED",
                        "coveredClasses": [0]
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
                        "class": 0,
                        "result": "PASSED",
                        "coveredClasses": [0],
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
                        "class": 0,
                        "result": "PASSED",
                        "coveredClasses": [0],
                        "executionId": "11111111111111111111111111111111"
                    }
                ]
            }
        """);
        var mergedAnalysis = baseline.merge(newAnalysis);
        assertThat(mergedAnalysis.toJson()).isEqualToIgnoringWhitespace("""
            {
                "id": "021C7D48FCEF65129C3F898D3DA393D3",
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
                        "class": 0,
                        "result": "PASSED",
                        "coveredClasses": [0],
                        "executionId": "11111111111111111111111111111111"
                    }
                ]
            }
        """);
    }

}