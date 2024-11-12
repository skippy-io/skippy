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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClassFileTest {

    @Test
    void testToJsonAllProperties() {
        var classFile = new ClassFile(
                "com.example.RightPadder",
                Path.of("com/example/RightPadder.class"), Path.of("build/classes/java/main"),
                "ZT0GoiWG8Az5TevH9/JwBg=="
        );

        assertThat(classFile.toJson()).isEqualToIgnoringWhitespace(
            """
                {
                    "name": "com.example.RightPadder",
                    "path": "com/example/RightPadder.class",
                    "outputFolder": "build/classes/java/main",
                    "hash": "ZT0GoiWG8Az5TevH9/JwBg=="
                }
                """);
    }

    @Test
    void testParse() {
        var classFile = ClassFile.parse(new Tokenizer(
            """
                {
                    "name": "com.example.RightPadder",
                    "path": "com/example/RightPadder.class",
                    "outputFolder": "build/classes/java/main",
                    "hash": "ZT0GoiWG8Az5TevH9/JwBg=="
                }
                """
        ));
        assertEquals("com.example.RightPadder", classFile.getClassName());
        assertEquals(Path.of("com/example/RightPadder.class"), classFile.getPath());
        assertEquals(Path.of("build/classes/java/main"), classFile.getOutputFolder());
        assertEquals("ZT0GoiWG8Az5TevH9/JwBg==", classFile.getHash());
    }

    @ParameterizedTest
    @CsvSource(value = {
        "com/example/LeftPadder.class:com.example.LeftPadder",
        "com/example/LeftPadderTest.class:com.example.LeftPadderTest"
    }, delimiter = ':')
    void testGetClassName(String fileName, String expectedValue) {
        var classFile = Path.of(fileName);
        var outputFolder = Path.of("build/classes/java/test");
        var projectDir = Path.of(".");
        assertEquals(expectedValue, ClassFile.fromFileSystem(projectDir, outputFolder, projectDir.resolve(outputFolder).resolve(classFile)).getClassName());
    }

    @ParameterizedTest
    @CsvSource(value = {
        "com/example/LeftPadder.class:com/example/LeftPadder.class",
        "com/example/LeftPadderTest.class:com/example/LeftPadderTest.class"
    }, delimiter = ':')
    void testGetPath(String fileName, String expectedValue) {
        var classFile = Path.of(fileName);
        var outputFolder = Path.of("build/classes/java/test");
        var projectDir = Path.of(".");
        assertEquals(Path.of(expectedValue), ClassFile.fromFileSystem(projectDir, outputFolder, projectDir.resolve(outputFolder).resolve(classFile)).getPath());
    }

    @ParameterizedTest
    @CsvSource(value = {
        "com/example/LeftPadder.class:build/classes/java/test",
        "com/example/LeftPadderTest.class:build/classes/java/test"
    }, delimiter = ':')
    void testGetOutputFolder(String fileName, String expectedValue) {
        var classFile = Path.of(fileName);
        var outputFolder = Path.of("build/classes/java/test");
        var projectDir = Path.of(".");
        assertEquals(Path.of(expectedValue), ClassFile.fromFileSystem(projectDir, outputFolder, projectDir.resolve(outputFolder).resolve(classFile)).getOutputFolder());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "com/example/LeftPadder.class:8E994DD8",
            "com/example/LeftPadderTest.class:80E52EBA"
    }, delimiter = ':')
    void getHash(String fileName, String expectedValue) {
        var classFile = Path.of(fileName);
        var outputFolder = Path.of("build/classes/java/test");
        var projectDir = Path.of(".");
        assertEquals(expectedValue, ClassFile.fromFileSystem(projectDir, outputFolder, projectDir.resolve(outputFolder).resolve(classFile)).getHash());
    }


}