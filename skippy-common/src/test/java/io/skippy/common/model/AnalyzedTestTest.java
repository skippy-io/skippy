package io.skippy.common.model;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnalyzedTestTest {

    @Test
    void testToJsonNoCoveredClasses() {
        var analyzedTest = new AnalyzedTest(
                ClassFile.fromParsedJson(
                    "com.example.LeftPadderTest",
                        Path.of("build/classes/java/test"), Path.of("com/example/LeftPadderTest.class"),
                        "ZT0GoiWG8Az5TevH9/JwBg=="
                ),
                TestResult.SUCCESS,
                emptyList()
        );

        assertThat(analyzedTest.toJson()).isEqualToIgnoringWhitespace(
            """
                {
                    "testClass": {
                        "class": "com.example.LeftPadderTest",
                        "path": "com/example/LeftPadderTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "ZT0GoiWG8Az5TevH9/JwBg=="
                    },
                    "result": "SUCCESS",
                    "coveredClasses": []
                }
                """);
    }

    @Test
    void testParseNoCoveredClasses() {
        var analyzedTest = AnalyzedTest.parse(new Tokenizer(
            """
                {
                    "testClass": {
                        "class": "com.example.LeftPadderTest",
                        "path": "com/example/LeftPadderTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "ZT0GoiWG8Az5TevH9/JwBg=="
                    },
                    "result": "SUCCESS",
                    "coveredClasses": []
                }
                """));

        assertEquals("com.example.LeftPadderTest", analyzedTest.test().getClassName());
        assertEquals(Path.of("com/example/LeftPadderTest.class"), analyzedTest.test().getClassFile());
        assertEquals(Path.of("build/classes/java/test"), analyzedTest.test().getOutputFolder());
        assertEquals("ZT0GoiWG8Az5TevH9/JwBg==", analyzedTest.test().getHash());
        assertEquals(TestResult.SUCCESS, analyzedTest.result());
        assertEquals(emptyList(), analyzedTest.coveredClasses());

    }

    @Test
    void testToJsonOneCoveredClass() {
        var analyzedTest = new AnalyzedTest(
                ClassFile.fromParsedJson(
                    "com.example.LeftPadderTest",
                        Path.of("build/classes/java/test"), Path.of("com/example/LeftPadderTest.class"),
                        "ZT0GoiWG8Az5TevH9/JwBg=="
                ),
                TestResult.SUCCESS,
                asList(ClassFile.fromParsedJson(
                        "com.example.LeftPadder",
                        Path.of("build/classes/java/main"), Path.of("com/example/LeftPadder.class"),
                        "ZT0GoiWG8Az5TevH9/JwBg=="
                    )
                )
        );

        assertThat(analyzedTest.toJson()).isEqualToIgnoringWhitespace(
            """
                {
                    "testClass": {
                        "class": "com.example.LeftPadderTest",
                        "path": "com/example/LeftPadderTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "ZT0GoiWG8Az5TevH9/JwBg=="
                    },
                    "result": "SUCCESS",
                    "coveredClasses": [
                        {
                            "class": "com.example.LeftPadder",
                            "path": "com/example/LeftPadder.class",
                            "outputFolder": "build/classes/java/main",
                            "hash": "ZT0GoiWG8Az5TevH9/JwBg=="
                        }
                    ]
                }
                """);
    }

    @Test
    void testParseOneCoveredClass() {
        var analyzedTest = AnalyzedTest.parse(new Tokenizer(
            """
                {
                    "testClass": {
                        "class": "com.example.LeftPadderTest",
                        "path": "com/example/LeftPadderTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "ZT0GoiWG8Az5TevH9/JwBg=="
                    },
                    "result": "SUCCESS",
                    "coveredClasses": [
                        {
                            "class": "com.example.LeftPadder",
                            "path": "com/example/LeftPadder.class",
                            "outputFolder": "build/classes/java/main",
                            "hash": "ZT0GoiWG8Az5TevH9/JwBg=="
                        }
                    ]
                }
                """));

        assertEquals("com.example.LeftPadderTest", analyzedTest.test().getClassName());
        assertEquals(1, analyzedTest.coveredClasses().size());
        assertEquals("com.example.LeftPadder", analyzedTest.coveredClasses().get(0).getClassName());
    }

    @Test
    void testToJsonTwoCoveredClasses() {
        var analyzedTest = new AnalyzedTest(
                ClassFile.fromParsedJson(
                    "com.example.LeftPadderTest",
                        Path.of("build/classes/java/test"), Path.of("com/example/LeftPadderTest.class"),
                        "ZT0GoiWG8Az5TevH9/JwBg=="
                ),
                TestResult.SUCCESS,
                asList(
                    ClassFile.fromParsedJson(
                        "com.example.LeftPadder",
                            Path.of("build/classes/java/main"), Path.of("com/example/LeftPadder.class"),
                            "ZT0GoiWG8Az5TevH9/JwBg=="
                    ),
                    ClassFile.fromParsedJson(
                        "com.example.StringUtils",
                            Path.of("build/classes/java/main"), Path.of("com/example/StringUtils.class"),
                            "4VP9fWGFUJHKIBG47OXZTQ==")
                    )
        );
        assertThat(analyzedTest.toJson()).isEqualToIgnoringWhitespace(
                """
                    {
                        "testClass": {
                            "class": "com.example.LeftPadderTest",
                            "path": "com/example/LeftPadderTest.class",
                            "outputFolder": "build/classes/java/test",
                            "hash": "ZT0GoiWG8Az5TevH9/JwBg=="
                        },
                        "result": "SUCCESS",
                        "coveredClasses": [
                            {
                                "class": "com.example.LeftPadder",
                                "path": "com/example/LeftPadder.class",
                                "outputFolder": "build/classes/java/main",
                                "hash": "ZT0GoiWG8Az5TevH9/JwBg=="
                            },
                            {
                                "class": "com.example.StringUtils",
                                "path": "com/example/StringUtils.class",
                                "outputFolder": "build/classes/java/main",
                                "hash": "4VP9fWGFUJHKIBG47OXZTQ=="
                            }
                        ]
                    }
                    """);
    }

    @Test
    void testParseTwoCoveredClasses() {
        var analyzedTest = AnalyzedTest.parse(new Tokenizer(
                """
                    {
                        "testClass": {
                            "class": "com.example.LeftPadderTest",
                            "path": "com/example/LeftPadderTest.class",
                            "outputFolder": "build/classes/java/test",
                            "hash": "ZT0GoiWG8Az5TevH9/JwBg=="
                        },
                        "result": "SUCCESS",
                        "coveredClasses": [
                            {
                                "class": "com.example.LeftPadder",
                                "path": "com/example/LeftPadder.class",
                                "outputFolder": "build/classes/java/main",
                                "hash": "ZT0GoiWG8Az5TevH9/JwBg=="
                            },
                            {
                                "class": "com.example.StringUtils",
                                "path": "com/example/StringUtils.class",
                                "outputFolder": "build/classes/java/main",
                                "hash": "4VP9fWGFUJHKIBG47OXZTQ=="
                            }
                        ]
                    }
                    """));

        assertEquals("com.example.LeftPadderTest", analyzedTest.test().getClassName());
        assertEquals(2, analyzedTest.coveredClasses().size());
        assertEquals("com.example.LeftPadder", analyzedTest.coveredClasses().get(0).getClassName());
        assertEquals("com.example.StringUtils", analyzedTest.coveredClasses().get(1).getClassName());
    }


}