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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClassFileContainerTest {

    @Test
    void testFrom() {

        var classes = asList(
                new ClassFile("com.example.FooTest", Path.of("com/example/FooTest.class"), Path.of("build/classes/java/test"), "hash-foo-test"),
                new ClassFile("com.example.BarTest", Path.of("com/example/BarTest.class"), Path.of("build/classes/java/test"), "hash-bar-test"),
                new ClassFile("com.example.Foo", Path.of("com/example/Foo.class"), Path.of("build/classes/java/main"), "hash-foo"),
                new ClassFile("com.example.Bar", Path.of("com/example/Bar.class"), Path.of("build/classes/java/main"), "hash-bar")
        );

        var classFileContainer = ClassFileContainer.from(classes);

        assertThat(classFileContainer.toJson()).isEqualToIgnoringWhitespace("""
            {
                "0": {
                    "name": "com.example.Bar",
                    "path": "com/example/Bar.class",
                    "outputFolder": "build/classes/java/main",
                    "hash": "hash-bar"
                },
                "1": {
                    "name": "com.example.BarTest",
                    "path": "com/example/BarTest.class",
                    "outputFolder": "build/classes/java/test",
                    "hash": "hash-bar-test"
                },
                "2": {
                    "name": "com.example.Foo",
                    "path": "com/example/Foo.class",
                    "outputFolder": "build/classes/java/main",
                    "hash": "hash-foo"
                },
                "3": {
                    "name": "com.example.FooTest",
                    "path": "com/example/FooTest.class",
                    "outputFolder": "build/classes/java/test",
                    "hash": "hash-foo-test"
                }
            }
        """);
    }

    @Test
    void testParse() {
        var classFileContainer = ClassFileContainer.parse(new Tokenizer(
            """
                {
                    "0": {
                        "name": "com.example.LeftPadder",
                        "path": "io/skippy/core/LeftPadder.class",
                        "outputFolder": "src/main/resources",
                        "hash": "9U3+WYit7uiiNqA9jplN2A=="
                    },
                    "1": {
                        "name": "com.example.LeftPadderTest",
                        "path": "io/skippy/core/LeftPadderTest.class",
                        "outputFolder": "src/test/resources",
                        "hash": "sGLJTZJw4beE9m2Kg6chUg=="                        
                    }
                }
            """));
        assertEquals(2, classFileContainer.getClassFiles().size());
        assertEquals("com.example.LeftPadder", classFileContainer.getById(0).getClassName());
        assertEquals("com.example.LeftPadderTest", classFileContainer.getById(1).getClassName());
    }

    @Test
    void testMergeNewClass() {
        var baseline = ClassFileContainer.parse(new Tokenizer(
                """
                    {
                        "0": {
                            "name": "com.example.Foo",
                            "path": "com.example.Foo.class",
                            "outputFolder": "src/main/java",
                            "hash": "Foo#hash"
                        }
                    }
                """));
        var other = ClassFileContainer.parse(new Tokenizer(
                """
                    {
                        "0": {
                            "name": "com.example.Bar",
                            "path": "com.example.Bar.class",
                            "outputFolder": "src/main/java",
                            "hash": "Bar#hash"
                        }
                    }
                """));

        var merged = baseline.merge(other);
        assertThat(merged.toJson()).isEqualToIgnoringWhitespace("""
            {
                "0": {
                    "name": "com.example.Bar",
                    "path": "com.example.Bar.class",
                    "outputFolder": "src/main/java",
                    "hash": "Bar#hash"
                },
                "1": {
                    "name": "com.example.Foo",
                    "path": "com.example.Foo.class",
                    "outputFolder": "src/main/java",
                    "hash": "Foo#hash"
                }
            }
        """);
    }

    @Test
    void testMergeUpdatedClass() {
        var baseline = ClassFileContainer.parse(new Tokenizer(
                """
                    {
                        "0": {
                            "name": "com.example.Foo",
                            "path": "com.example.Foo.class",
                            "outputFolder": "src/main/java",
                            "hash": "Foo#hash"
                        }
                    }
                """));
        var other = ClassFileContainer.parse(new Tokenizer(
                """
                    {
                        "0": {
                            "name": "com.example.Foo",
                            "path": "com.example.Foo.class",
                            "outputFolder": "src/main/java",
                            "hash": "Foo#hash-modified"
                        }
                    }
                """));

        var merged = baseline.merge(other);
        assertThat(merged.toJson()).isEqualToIgnoringWhitespace("""
            {
                "0": {
                    "name": "com.example.Foo",
                    "path": "com.example.Foo.class",
                    "outputFolder": "src/main/java",
                    "hash": "Foo#hash-modified"
                }
            }
        """);
    }

}
