package io.skippy.common.model;

import org.junit.jupiter.api.Test;

import java.util.Optional;

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
    void testToJsonWithExecutionData() {
        var analyzedTest = new AnalyzedTest("0", TestResult.PASSED, asList(), "C57F877F6F9BF164");

        assertThat(analyzedTest.toJson()).isEqualToIgnoringWhitespace("""
            {
                "class": "0",
                "result": "PASSED",
                "coveredClasses": [],
                "executionDataRef": "C57F877F6F9BF164"
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
        assertEquals(Optional.empty(), analyzedTest.jacocoExecutionDataRef());
    }

    @Test
    void testParseWithExecutionData() {
        var analyzedTest = AnalyzedTest.parse(new Tokenizer("""
            {
                "class": "0",
                "result": "PASSED",
                "coveredClasses": [],
                "executionDataRef": "B062C94D9270E1B784F66D8A83A72152"
            }
        """));

        assertEquals("0", analyzedTest.testClassId());
        assertEquals(TestResult.PASSED, analyzedTest.result());
        assertEquals(asList(), analyzedTest.coveredClassesIds());
        assertEquals("B062C94D9270E1B784F66D8A83A72152", analyzedTest.jacocoExecutionDataRef().get());
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

}