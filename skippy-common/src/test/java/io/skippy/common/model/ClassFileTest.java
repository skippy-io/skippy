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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClassFileTest {

    @Test
    void testToJsonAllProperties() {
        var classFile = ClassFile.fromParsedJson(
                "com.example.RightPadder",
                Path.of("build/classes/java/main"), Path.of("com/example/RightPadder.class"),
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
    void testToJsonSingleProperty() {
        var classFile = ClassFile.fromParsedJson(
                "com.example.RightPadder",
                Path.of("build/classes/java/main"), Path.of("com/example/RightPadder.class"),
                "ZT0GoiWG8Az5TevH9/JwBg=="
        );

        assertThat(classFile.toJson(asList(JsonConfiguration.Classes.NAME))).isEqualToIgnoringWhitespace(
                """
                    {
                        "name": "com.example.RightPadder"
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
        assertEquals(Path.of("com/example/RightPadder.class"), classFile.getClassFile());
        assertEquals(Path.of("build/classes/java/main"), classFile.getOutputFolder());
        assertEquals("ZT0GoiWG8Az5TevH9/JwBg==", classFile.getHash());
    }


    @ParameterizedTest
    @CsvSource(value = {
            "LeftPadder.class:com.example.LeftPadder",
            "LeftPadderTest.class:com.example.LeftPadderTest"
    }, delimiter = ':')
    void testGetClassName(String fileName, String expectedValue) throws URISyntaxException {
        var classFile = Paths.get(getClass().getResource(fileName).toURI());
        var outputFolder = classFile.getParent();
        var projectDir = outputFolder.getParent();
        assertEquals(expectedValue, ClassFile.fromFileSystem(projectDir, outputFolder, classFile).getClassName());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "LeftPadder.class:LeftPadder.class",
            "LeftPadderTest.class:LeftPadderTest.class"
    }, delimiter = ':')
    void testGetClassFile(String fileName, String expectedValue) throws URISyntaxException {
        var classFile = Paths.get(getClass().getResource(fileName).toURI());
        var outputFolder = classFile.getParent();
        var projectDir = outputFolder.getParent();
        assertEquals(Path.of(expectedValue), ClassFile.fromFileSystem(projectDir, outputFolder, classFile).getClassFile());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "LeftPadder.class:model",
            "LeftPadderTest.class:model"
    }, delimiter = ':')
    void testGetOutputFolder(String fileName, String expectedValue) throws URISyntaxException {
        var classFile = Paths.get(getClass().getResource(fileName).toURI());
        var outputFolder = classFile.getParent();
        var projectDir = outputFolder.getParent();
        assertEquals(Path.of(expectedValue), ClassFile.fromFileSystem(projectDir, outputFolder, classFile).getOutputFolder());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "LeftPadder.class:8E994DD8",
            "LeftPadderTest.class:83A72152"
    }, delimiter = ':')
    void getHash(String fileName, String expectedValue) throws URISyntaxException {
        var classFile = Paths.get(getClass().getResource(fileName).toURI());
        var outputFolder = classFile.getParent();
        var projectDir = outputFolder.getParent();
        assertEquals(expectedValue, ClassFile.fromFileSystem(projectDir, outputFolder, classFile).getHash());
    }


}