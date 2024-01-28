package io.skippy.common.model;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestImpactAnalysisTest {

    @Test
    void testToJsonNoTests() {
        var testImpactAnalysis = new TestImpactAnalysis(emptyList());
        assertThat(testImpactAnalysis.toJson()).isEqualToIgnoringWhitespace(
        """
                []
                """);
    }

    @Test
    void testParseNoTests() {
        var testImpactAnalysis = TestImpactAnalysis.parse(
            """
            []
            """);
        assertEquals(emptyList(), testImpactAnalysis.getAnalyzedTests());
    }

    @Test
    void testToJsonOneTest() {
        var test = new AnalyzedTest(
                ClassFile.fromParsedJson(
                    "com.example.FooTest",
                        Path.of("build/classes/java/test"), Path.of("com/example/FooTest.class"),
                        "ZT0GoiWG8Az5TevH9/JwBg=="
                ),
                TestResult.SUCCESS,
                emptyList()
        );
        var testImpactAnalysis = new TestImpactAnalysis(asList(test));
        assertThat(testImpactAnalysis.toJson()).isEqualToIgnoringWhitespace(
            """
                [
                    {
                        "testClass": {
                            "class": "com.example.FooTest",
                            "path": "com/example/FooTest.class",
                            "outputFolder": "build/classes/java/test",
                            "hash": "ZT0GoiWG8Az5TevH9/JwBg=="
                        },
                        "result": "SUCCESS",
                        "coveredClasses": []
                    }
                ]
                """);
    }

    @Test
    void testParseOneTests() {
        var testImpactAnalysis = TestImpactAnalysis.parse(
            """
                [
                    {
                        "testClass":{
                            "class": "com.example.FooTest",
                            "path": "com/example/FooTest.class",
                            "outputFolder": "build/classes/java/test",
                            "hash": "ZT0GoiWG8Az5TevH9/JwBg=="
                        },
                        "result": "SUCCESS",
                        "coveredClasses": []
                    }
                ]
                """);
        assertEquals(1, testImpactAnalysis.getAnalyzedTests().size());
        assertEquals("com.example.FooTest", testImpactAnalysis.getAnalyzedTests().get(0).test().getClassName());
    }


    @Test
    void testToJsonTwoTests() {
        var test1 = new AnalyzedTest(
                ClassFile.fromParsedJson(
                    "com.example.FooTest",
                        Path.of("build/classes/java/test"), Path.of("com/example/FooTest.class"),
                        "ZT0GoiWG8Az5TevH9/JwBg=="
                ),
                TestResult.SUCCESS,
                emptyList()
        );
        var test2 = new AnalyzedTest(
                ClassFile.fromParsedJson(
                    "com.example.BarTest",
                        Path.of("build/classes/java/test"), Path.of("com/example/BarTest.class"),
                        "ZT0GoiWG8Az5TevH9/JwBg=="
                ),
                TestResult.SUCCESS,
                emptyList()
        );
        var testImpactAnalysis = new TestImpactAnalysis(asList(test1, test2));
        assertThat(testImpactAnalysis.toJson()).isEqualToIgnoringWhitespace(
            """
                [
                    {
                        "testClass": {
                            "class": "com.example.FooTest",
                            "path": "com/example/FooTest.class",
                            "outputFolder": "build/classes/java/test",
                            "hash": "ZT0GoiWG8Az5TevH9/JwBg=="
                        },
                        "result": "SUCCESS",
                        "coveredClasses": []
                    },
                    {
                        "testClass": {
                            "class": "com.example.BarTest",
                            "path": "com/example/BarTest.class",
                            "outputFolder": "build/classes/java/test",
                            "hash": "ZT0GoiWG8Az5TevH9/JwBg=="
                        },
                        "result": "SUCCESS",
                        "coveredClasses": []
                    }
                ]
                """);
    }

    @Test
    void testParseTwoTests() {
        var testImpactAnalysis = TestImpactAnalysis.parse(
            """
                [
                    {
                        "testClass": {
                            "class": "com.example.FooTest",
                            "path": "com/example/FooTest.class",
                            "outputFolder": "build/classes/java/test",
                            "hash": "ZT0GoiWG8Az5TevH9/JwBg=="
                        },
                        "result": "SUCCESS",
                        "coveredClasses": []
                    },
                    {
                        "testClass": {
                            "class": "com.example.BarTest",
                            "path": "com/example/BarTest.class",
                            "outputFolder": "build/classes/java/test",
                            "hash": "ZT0GoiWG8Az5TevH9/JwBg=="
                        },
                        "result": "SUCCESS",
                        "coveredClasses": []
                    }
                ]
            """);
        assertEquals(2, testImpactAnalysis.getAnalyzedTests().size());
        assertEquals("com.example.FooTest", testImpactAnalysis.getAnalyzedTests().get(0).test().getClassName());
        assertEquals("com.example.BarTest", testImpactAnalysis.getAnalyzedTests().get(1).test().getClassName());
    }

}
