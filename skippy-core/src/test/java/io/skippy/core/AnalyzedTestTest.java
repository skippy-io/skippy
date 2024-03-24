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

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.Optional;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnalyzedTestTest {

    @Test
    void testToJsonNoCoveredClasses() throws JSONException {
        var analyzedTest = new AnalyzedTest("0", TestResult.PASSED, asList(), Optional.empty());

        var expected = """
            {
                "class": "0",
                "result": "PASSED",
                "coveredClasses": []
            }
        """;
        JSONAssert.assertEquals(expected, analyzedTest.toJson(), JSONCompareMode.LENIENT);
    }

    @Test
    void testToJsonOneCoveredClass() throws JSONException {
        var analyzedTest = new AnalyzedTest("0", TestResult.PASSED, asList("0"), Optional.empty());
        var expected = """
            {
                "class": "0",
                "result": "PASSED",
                "coveredClasses": ["0"]
            }
        """;
        JSONAssert.assertEquals(expected, analyzedTest.toJson(), JSONCompareMode.LENIENT);
    }
    @Test
    void testToJsonTwoCoveredClasses() throws JSONException {
        var analyzedTest = new AnalyzedTest("0", TestResult.PASSED, asList("0", "1"), Optional.empty());
        var expected = """
            {
                "class": "0",
                "result": "PASSED",
                "coveredClasses": ["0", "1"]
            }
        """;
        JSONAssert.assertEquals(expected, analyzedTest.toJson(), JSONCompareMode.LENIENT);
    }

    @Test
    void testToJsonFailedTest() throws JSONException {
        var analyzedTest = new AnalyzedTest("0", TestResult.FAILED, asList(), Optional.empty());
        var expected = """
            {
                "class": "0",
                "result": "FAILED",
                "coveredClasses": []
            }
        """;
        JSONAssert.assertEquals(expected, analyzedTest.toJson(), JSONCompareMode.LENIENT);
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

        assertEquals("0", analyzedTest.getTestClassId());
        assertEquals(TestResult.PASSED, analyzedTest.getResult());
        assertEquals(asList(), analyzedTest.getCoveredClassesIds());
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
        assertEquals(asList("0"), analyzedTest.getCoveredClassesIds());
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
        assertEquals(asList("0", "1"), analyzedTest.getCoveredClassesIds());
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
        assertEquals(TestResult.FAILED, analyzedTest.getResult());
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
        assertEquals(Optional.empty(), analyzedTest.getExecutionId());
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
        assertEquals("00000000000000000000000000000000", analyzedTest.getExecutionId().get());
    }

}