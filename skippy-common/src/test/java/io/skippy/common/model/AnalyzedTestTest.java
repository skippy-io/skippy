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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnalyzedTestTest {

    @Test
    void testToJsonNoCoveredClasses() {
        var analyzedTest = new AnalyzedTest("0", TestResult.PASSED, asList(), "0".repeat(32));

        assertThat(analyzedTest.toJson()).isEqualToIgnoringWhitespace("""
            {
                "class": "0",
                "result": "PASSED",
                "coveredClasses": [],
                "execution": "00000000000000000000000000000000"
            }
        """);
    }

    @Test
    void testToJsonWithExecutionData() {
        var analyzedTest = new AnalyzedTest("0", TestResult.PASSED, asList(), "C57F877F6F9BF164");

        assertThat(analyzedTest.toJson()).isEqualToIgnoringWhitespace("""
            {
                "class": "0",
                "result": "PASSED",
                "coveredClasses": [],
                "execution": "C57F877F6F9BF164"
            }
        """);
    }

    @Test
    void testToJsonOneCoveredClass() {
        var analyzedTest = new AnalyzedTest("0", TestResult.PASSED, asList("0"), "0".repeat(32));

        assertThat(analyzedTest.toJson()).isEqualToIgnoringWhitespace("""
            {
                "class": "0",
                "result": "PASSED",
                "coveredClasses": ["0"],
                "execution": "00000000000000000000000000000000"
            }
        """);
    }
    @Test
    void testToJsonTwoCoveredClasses() {
        var analyzedTest = new AnalyzedTest("0", TestResult.PASSED, asList("0", "1"), "0".repeat(32));

        assertThat(analyzedTest.toJson()).isEqualToIgnoringWhitespace("""
            {
                "class": "0",
                "result": "PASSED",
                "coveredClasses": ["0", "1"],
                "execution": "00000000000000000000000000000000"
            }
        """);
    }

    @Test
    void testToJsonFailedTest() {
        var analyzedTest = new AnalyzedTest("0", TestResult.FAILED, asList(), "0".repeat(32));

        assertThat(analyzedTest.toJson()).isEqualToIgnoringWhitespace("""
            {
                "class": "0",
                "result": "FAILED",
                "coveredClasses": [],
                "execution": "00000000000000000000000000000000"
            }
        """);
    }

    @Test
    void testParseNoCoveredClasses() {
        var analyzedTest = AnalyzedTest.parse(new Tokenizer("""
            {
                "class": "0",
                "result": "PASSED",
                "coveredClasses": [],
                "execution": "B062C94D9270E1B784F66D8A83A72152"
            }
        """));

        assertEquals("0", analyzedTest.testClassId());
        assertEquals(TestResult.PASSED, analyzedTest.result());
        assertEquals(asList(), analyzedTest.coveredClassesIds());
        assertEquals("B062C94D9270E1B784F66D8A83A72152", analyzedTest.execution());
    }

    @Test
    void testParseOneCoveredClass() {
        var analyzedTest = AnalyzedTest.parse(new Tokenizer("""
            {
                "class": "0",
                "result": "PASSED",
                "coveredClasses": ["0"],
                "execution": "B062C94D9270E1B784F66D8A83A72152"
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
                "coveredClasses": ["0", "1"],
                "execution": "B062C94D9270E1B784F66D8A83A72152"
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
                "coveredClasses": [],
                "execution": "B062C94D9270E1B784F66D8A83A72152"
            }
        """));
        assertEquals(TestResult.FAILED, analyzedTest.result());
    }

}