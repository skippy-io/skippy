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

import java.util.Optional;

import static io.skippy.common.model.AnalyzedTest.JsonProperty.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnalyzedTestTest {

    @Test
    void testToJsonNoCoveredClasses() {
        var analyzedTest = new AnalyzedTest("0", TestResult.PASSED, asList());

        assertThat(analyzedTest.toJson()).isEqualToIgnoringWhitespace("""
            {
                "class": "0",
                "result": "PASSED",
                "coveredClasses": []
            }
        """);
    }

    @Test
    void testToJsonOneCoveredClass() {
        var analyzedTest = new AnalyzedTest("0", TestResult.PASSED, asList("0"));

        assertThat(analyzedTest.toJson()).isEqualToIgnoringWhitespace("""
            {
                "class": "0",
                "result": "PASSED",
                "coveredClasses": ["0"]
            }
        """);
    }
    @Test
    void testToJsonTwoCoveredClasses() {
        var analyzedTest = new AnalyzedTest("0", TestResult.PASSED, asList("0", "1"));

        assertThat(analyzedTest.toJson()).isEqualToIgnoringWhitespace("""
            {
                "class": "0",
                "result": "PASSED",
                "coveredClasses": ["0", "1"]
            }
        """);
    }

    @Test
    void testToJsonFailedTest() {
        var analyzedTest = new AnalyzedTest("0", TestResult.FAILED, asList());

        assertThat(analyzedTest.toJson()).isEqualToIgnoringWhitespace("""
            {
                "class": "0",
                "result": "FAILED",
                "coveredClasses": []
            }
        """);
    }

    @Test
    void testParseNoCoveredClasses() {
        var analyzedTest = AnalyzedTest.parse(new Tokenizer("""
            {
                "class": "0",
                "result": "PASSED",
                "coveredClasses": []
            }
        """));

        assertEquals("0", analyzedTest.testClassId());
        assertEquals(TestResult.PASSED, analyzedTest.result());
        assertEquals(asList(), analyzedTest.coveredClassesIds());
    }

    @Test
    void testParseOneCoveredClass() {
        var analyzedTest = AnalyzedTest.parse(new Tokenizer("""
            {
                "class": "0",
                "result": "PASSED",
                "coveredClasses": ["0"]
            }
        """));
        assertEquals(asList("0"), analyzedTest.coveredClassesIds());
    }

    @Test
    void testParseTwoCoveredClasses() {
        var analyzedTest = AnalyzedTest.parse(new Tokenizer("""
            {
                "class": "0",
                "result": "PASSED",
                "coveredClasses": ["0", "1"]
            }
        """));
        assertEquals(asList("0", "1"), analyzedTest.coveredClassesIds());
    }

    @Test
    void testParseFailedTest() {
        var analyzedTest = AnalyzedTest.parse(new Tokenizer("""
            {
                "class": "0",
                "result": "FAILED",
                "coveredClasses": []
            }
        """));
        assertEquals(TestResult.FAILED, analyzedTest.result());
    }

    @Test
    void testParseWithoutExecutionId() {
        var analyzedTest = AnalyzedTest.parse(new Tokenizer("""
            {
                "class": "0",
                "result": "FAILED",
                "coveredClasses": []
            }
        """));
        assertEquals(Optional.empty(), analyzedTest.executionId());
    }

    @Test
    void testParseWithExecutionId() {
        var analyzedTest = AnalyzedTest.parse(new Tokenizer("""
            {
                "class": "0",
                "result": "FAILED",
                "coveredClasses": [],
                "executionId":  "00000000000000000000000000000000"
            }
        """));
        assertEquals("00000000000000000000000000000000", analyzedTest.executionId().get());
    }

    @Test
    void testToJsonClassProperty() {
        var analyzedTest = new AnalyzedTest("0", TestResult.PASSED, asList("0", "1"));

        assertThat(analyzedTest.toJson(CLASS)).isEqualToIgnoringWhitespace("""
            {
                "class": "0"
            }
        """);
    }

    @Test
    void testToJsonResultProperty() {
        var analyzedTest = new AnalyzedTest("0", TestResult.PASSED, asList("0", "1"));

        assertThat(analyzedTest.toJson(RESULT)).isEqualToIgnoringWhitespace("""
            {
                "result": "PASSED"
            }
        """);
    }

    @Test
    void testToJsonCoveredClassesProperty() {
        var analyzedTest = new AnalyzedTest("0", TestResult.PASSED, asList("0", "1"));

        assertThat(analyzedTest.toJson(COVERED_CLASSES)).isEqualToIgnoringWhitespace("""
            {
                "coveredClasses": ["0", "1"]
            }
        """);
    }
    @Test
    void testToJsonExecutionProperty() {
        var analyzedTest = new AnalyzedTest("0", TestResult.PASSED, asList("0", "1"), "0".repeat(32));

        assertThat(analyzedTest.toJson(EXECUTION)).isEqualToIgnoringWhitespace("""
            {
                "executionId": "00000000000000000000000000000000"
            }
        """);
    }
}