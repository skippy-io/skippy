package io.skippy.common.model;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestImpactAnalysisTest {

    @Test
    void testToJsonNoClassesNoTests() {
        var testImpactAnalysis = new TestImpactAnalysis(ClassFileContainer.from(emptyList()), emptyList());
        assertThat(testImpactAnalysis.toJson()).isEqualToIgnoringWhitespace("""
            {
                "classes": {
                },
                "tests": [
                ]
            }
        """);
    }

    @Test
    void testToJsonOneTestOneClass() {
        var fooTest = ClassFile.fromParsedJson(
                "com.example.FooTest",
                Path.of("build/classes/java/test"),
                Path.of("com/example/FooTest.class"),
                "ZT0GoiWG8Az5TevH9/JwBg=="
        );
        var testImpactAnalysis = new TestImpactAnalysis(
                ClassFileContainer.from(asList(fooTest)),
                asList(new AnalyzedTest("0", TestResult.PASSED, asList("0")))
        );
        assertThat(testImpactAnalysis.toJson()).isEqualToIgnoringWhitespace("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.FooTest",
                        "path": "com/example/FooTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "ZT0GoiWG8Az5TevH9/JwBg=="
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
    void testToJsonTwoTestsFourClasses() {
        var class1 = ClassFile.fromParsedJson(
                "com.example.Class1",
                Path.of("build/classes/java/main"),
                Path.of("com/example/Class1.class"),
                "class-1-hash"
        );
        var class1Test = ClassFile.fromParsedJson(
                "com.example.Class1Test",
                Path.of("build/classes/java/test"),
                Path.of("com/example/Class1Test.class"),
                "class-1-test-hash"
        );
        var class2 = ClassFile.fromParsedJson(
                "com.example.Class2",
                Path.of("build/classes/java/main"),
                Path.of("com/example/Class2.class"),
                "class-2-hash"
        );
        var class2Test = ClassFile.fromParsedJson(
                "com.example.Class2Test",
                Path.of("build/classes/java/test"),
                Path.of("com/example/Class2Test.class"),
                "class-2-test-hash"
        );
        var testImpactAnalysis = new TestImpactAnalysis(
                ClassFileContainer.from(asList(class1, class2, class1Test, class2Test)),
                asList(
                        new AnalyzedTest("1", TestResult.PASSED, asList("0", "1")),
                        new AnalyzedTest("2", TestResult.PASSED, asList("2", "3"))
                )
        );
        assertThat(testImpactAnalysis.toJson()).isEqualToIgnoringWhitespace("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.Class1",
                        "path": "com/example/Class1.class",
                        "outputFolder": "build/classes/java/main",
                        "hash": "class-1-hash"
                    },
                    "1": {
                        "name": "com.example.Class1Test",
                        "path": "com/example/Class1Test.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "class-1-test-hash"
                    },
                    "2": {
                        "name": "com.example.Class2",
                        "path": "com/example/Class2.class",
                        "outputFolder": "build/classes/java/main",
                        "hash": "class-2-hash"
                    },
                    "3": {
                        "name": "com.example.Class2Test",
                        "path": "com/example/Class2Test.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "class-2-test-hash"
                    }
                },
                "tests": [
                    {
                        "class": "1",
                        "result": "PASSED",
                        "coveredClasses": ["0","1"]
                    },
                    {
                        "class": "2",
                        "result": "PASSED",
                        "coveredClasses": ["2","3"]
                    }
                ]
            }
        """);
    }

    @Test
    void testParseNoClassesNoTests() {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {
                },
                "tests": [
                ]
            }
        """);

        assertEquals(emptyList(), testImpactAnalysis.getAnalyzedTests());
        assertEquals(emptySet(), testImpactAnalysis.getClassFileContainer().getClassFiles());
    }

    @Test
    void testParseOneTestOneClass() {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.FooTest",
                        "path": "com/example/FooTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "ZT0GoiWG8Az5TevH9/JwBg=="
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

        var classFiles = new ArrayList<>(testImpactAnalysis.getClassFileContainer().getClassFiles());
        assertEquals(1, classFiles.size());
        assertEquals("com.example.FooTest", classFiles.get(0).getClassName());

        var tests = testImpactAnalysis.getAnalyzedTests();
        assertEquals(1, tests.size());
        assertEquals("0", tests.get(0).testClassId());
        assertEquals(TestResult.PASSED, tests.get(0).result());
        assertEquals(asList("0"), tests.get(0).coveredClassesIds());
    }

    @Test
    void testParseTwoTestsFourClasses() {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.Class1",
                        "path": "com/example/Class1.class",
                        "outputFolder": "build/classes/java/main",
                        "hash": "class-1-hash"
                    },
                    "1": {
                        "name": "com.example.Class2",
                        "path": "com/example/Class2.class",
                        "outputFolder": "build/classes/java/main",
                        "hash": "class-2-hash"
                    },
                    "2": {
                        "name": "com.example.Class1Test",
                        "path": "com/example/Class1Test.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "class-1-test-hash"
                    },
                    "3": {
                        "name": "com.example.Class2Test",
                        "path": "com/example/Class2Test.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "class-2-test-hash"
                    }
                },
                "tests": [
                    {
                        "class": "2",
                        "result": "PASSED",
                        "coveredClasses": ["0","2"]
                    },
                    {
                        "class": "3",
                        "result": "PASSED",
                        "coveredClasses": ["1","3"]
                    }
                ]
            }
        """);

        var classFiles = new ArrayList<>(testImpactAnalysis.getClassFileContainer().getClassFiles());
        assertEquals(4, classFiles.size());
        assertEquals("com.example.Class1", classFiles.get(0).getClassName());
        assertEquals("com.example.Class1Test", classFiles.get(1).getClassName());
        assertEquals("com.example.Class2", classFiles.get(2).getClassName());
        assertEquals("com.example.Class2Test", classFiles.get(3).getClassName());

        var tests = testImpactAnalysis.getAnalyzedTests();
        assertEquals(2, tests.size());

        assertEquals("2", tests.get(0).testClassId());
        assertEquals(TestResult.PASSED, tests.get(0).result());
        assertEquals(asList("0", "2"), tests.get(0).coveredClassesIds());

        assertEquals("3", tests.get(1).testClassId());
        assertEquals(TestResult.PASSED, tests.get(1).result());
        assertEquals(asList("1", "3"), tests.get(1).coveredClassesIds());
    }

}