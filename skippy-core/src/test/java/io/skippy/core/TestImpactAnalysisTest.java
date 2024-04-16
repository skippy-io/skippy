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

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;

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
                "id": "F8D85DB143EC3F06FAD5D0E0C730E1E9",
                "classes": {},
                "tests": []
            }
        """);
    }

    @Test
    void testToJsonOneTestOneClass() {
        var fooTest = new ClassFile(
                "com.example.FooTest",
                Path.of("com/example/FooTest.class"),
                Path.of("build/classes/java/test"),
                "ZT0GoiWG8Az5TevH9/JwBg=="
        );
        var testImpactAnalysis = new TestImpactAnalysis(
                ClassFileContainer.from(asList(fooTest)),
                asList(new AnalyzedTest(0, TestResult.PASSED, asList(0), Optional.empty()))
        );
        assertThat(testImpactAnalysis.toJson()).isEqualToIgnoringWhitespace("""
            {
                "id": "D013368C0DD441D819DEA78640F4EC1A",
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
                        "class": 0,
                        "result": "PASSED",
                        "coveredClasses": [0]
                    }
                ]
            }
        """);
    }

    @Test
    void testToJsonTwoTestsFourClasses() {
        var class1 = new ClassFile(
                "com.example.Class1",
                Path.of("com/example/Class1.class"),
                Path.of("build/classes/java/main"),
                "class-1-hash"
        );
        var class1Test = new ClassFile(
                "com.example.Class1Test",
                Path.of("com/example/Class1Test.class"),
                Path.of("build/classes/java/test"),
                "class-1-test-hash"
        );
        var class2 = new ClassFile(
                "com.example.Class2",
                Path.of("com/example/Class2.class"),
                Path.of("build/classes/java/main"),
                "class-2-hash"
        );
        var class2Test = new ClassFile(
                "com.example.Class2Test",
                Path.of("com/example/Class2Test.class"),
                Path.of("build/classes/java/test"),
                "class-2-test-hash"
        );
        var testImpactAnalysis = new TestImpactAnalysis(
                ClassFileContainer.from(asList(class1, class2, class1Test, class2Test)),
                asList(
                        new AnalyzedTest(1, TestResult.PASSED, asList(0, 1), Optional.empty()),
                        new AnalyzedTest(2, TestResult.PASSED, asList(2, 3), Optional.empty())
                )
        );
        assertThat(testImpactAnalysis.toJson()).isEqualToIgnoringWhitespace("""
            {
                "id": "4473271405D844F7E9F969057E7FA479",
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
                        "class": 1,
                        "result": "PASSED",
                        "coveredClasses": [0,1]
                    },
                    {
                        "class": 2,
                        "result": "PASSED",
                        "coveredClasses": [2,3]
                    }
                ]
            }
        """);
    }

    @Test
    void testParseNoClassesNoTests() {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "id": "F8D85DB143EC3F06FAD5D0E0C730E1E9",
                "classes": {},
                "tests": []
            }
        """);

        assertEquals("F8D85DB143EC3F06FAD5D0E0C730E1E9", testImpactAnalysis.getId());
        assertEquals(emptyList(), testImpactAnalysis.getAnalyzedTests());
        assertEquals(emptySet(), testImpactAnalysis.getClassFileContainer().getClassFiles());
    }

    @Test
    void testParseOneTestOneClass() {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "id": "D013368C0DD441D819DEA78640F4EC1A",
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

        assertEquals("D013368C0DD441D819DEA78640F4EC1A", testImpactAnalysis.getId());

        var classFiles = new ArrayList<>(testImpactAnalysis.getClassFileContainer().getClassFiles());
        assertEquals(1, classFiles.size());
        assertEquals("com.example.FooTest", classFiles.get(0).getClassName());

        var tests = testImpactAnalysis.getAnalyzedTests();
        assertEquals(1, tests.size());
        assertEquals(0, tests.get(0).getTestClassId());
        assertEquals(TestResult.PASSED, tests.get(0).getResult());
        assertEquals(asList(0), tests.get(0).getCoveredClassesIds());
    }

    @Test
    void testParseTwoTestsFourClasses() {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "id": "40F512FD02B1EEBF932622C51AFB5268",
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

        assertEquals("40F512FD02B1EEBF932622C51AFB5268", testImpactAnalysis.getId());

        var classFiles = new ArrayList<>(testImpactAnalysis.getClassFileContainer().getClassFiles());
        assertEquals(4, classFiles.size());
        assertEquals("com.example.Class1", classFiles.get(0).getClassName());
        assertEquals("com.example.Class1Test", classFiles.get(1).getClassName());
        assertEquals("com.example.Class2", classFiles.get(2).getClassName());
        assertEquals("com.example.Class2Test", classFiles.get(3).getClassName());

        var tests = testImpactAnalysis.getAnalyzedTests();
        assertEquals(2, tests.size());

        assertEquals(2, tests.get(0).getTestClassId());
        assertEquals(TestResult.PASSED, tests.get(0).getResult());
        assertEquals(asList(0, 2), tests.get(0).getCoveredClassesIds());

        assertEquals(3, tests.get(1).getTestClassId());
        assertEquals(TestResult.PASSED, tests.get(1).getResult());
        assertEquals(asList(1, 3), tests.get(1).getCoveredClassesIds());
    }

}