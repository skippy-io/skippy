package io.skippy.common.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static java.util.Collections.emptyMap;
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

        assertThat(classFile.toJson(JsonProperty.CLASS_NAME)).isEqualToIgnoringWhitespace(
                """
                    {
                        "name": "com.example.RightPadder"
                    }
                    """);
    }
    @Test
    void testToTestClassJsonSingleProperty() {
        var classFile = ClassFile.fromParsedJson(
                "com.example.RightPadder",
                Path.of("build/classes/java/main"), Path.of("com/example/RightPadder.class"),
                "ZT0GoiWG8Az5TevH9/JwBg=="
        );

        assertThat(classFile.toTestClassJson(JsonProperty.CLASS_NAME)).isEqualToIgnoringWhitespace(
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
            "LeftPadder.class:9U3+WYit7uiiNqA9jplN2A==",
            "LeftPadderTest.class:sGLJTZJw4beE9m2Kg6chUg=="
    }, delimiter = ':')
    void getHash(String fileName, String expectedValue) throws URISyntaxException {
        var classFile = Paths.get(getClass().getResource(fileName).toURI());
        var outputFolder = classFile.getParent();
        var projectDir = outputFolder.getParent();
        assertEquals(expectedValue, ClassFile.fromFileSystem(projectDir, outputFolder, classFile).getHash());
    }


}