/*
 * Copyright 2023 the original author or authors.
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

package io.skippy.build;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link ClassFile}.
 *
 * @author Florian McKee
 */
public class ClassFileTest {

    @ParameterizedTest
    @CsvSource(value = {
            "SourceFileTest1.class:com.example.SourceFileTest1",
            "SourceFileTest2.class:com.example.SourceFileTest2"
    }, delimiter = ':')
    void testGetFullyQualifiedClassName(String fileName, String expectedValue) throws URISyntaxException {
        var classFile = Paths.get(getClass().getResource(fileName).toURI());
        assertEquals(expectedValue, new ClassFile(classFile).getFullyQualifiedClassName());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "SourceFileTest1.class:build/SourceFileTest1.class",
            "SourceFileTest2.class:build/SourceFileTest2.class"
    }, delimiter = ':')
    void testGetRelativePath(String fileName, String expectedValue) throws URISyntaxException {
        var classFile = Paths.get(getClass().getResource(fileName).toURI());
        assertEquals(Path.of(expectedValue), classFile.getParent().getParent().relativize(new ClassFile(classFile).getAbsolutePath()));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "SourceFileTest1.class:w7P+0X1Grw3y8nPkIceITQ==",
            "SourceFileTest2.class:V6NEvrWBtOEwb+4ZOEnrfw=="
    }, delimiter = ':')
    void getHash(String fileName, String expectedValue) throws URISyntaxException {
        var classFile = Paths.get(getClass().getResource(fileName).toURI());
        assertEquals(expectedValue, new ClassFile(classFile).getHash());
    }

}