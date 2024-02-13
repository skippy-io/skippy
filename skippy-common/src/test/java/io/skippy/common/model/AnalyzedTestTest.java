package io.skippy.common.model;

import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnalyzedTestTest {

    @Test
    void testToJsonNoCoveredClasses() {
        var analyzedTest = new AnalyzedTest("0", TestResult.PASSED, asList());

        assertThat(analyzedTest.toJson()).isEqualToIgnoringWhitespace(
        """
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

        assertThat(analyzedTest.toJson()).isEqualToIgnoringWhitespace(
                """
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

        assertThat(analyzedTest.toJson()).isEqualToIgnoringWhitespace(
                """
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

        assertThat(analyzedTest.toJson()).isEqualToIgnoringWhitespace(
        """
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

}