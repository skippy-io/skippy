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

package io.skippy.gradle.core;

import io.skippy.gradle.DecoratedClass;
import org.gradle.api.logging.Logger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.URISyntaxException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link DecoratedClass}.
 *
 * @author Florian McKee
 */
public class DecoratedClassTest {

    @ParameterizedTest
    @CsvSource(value = {
            "SourceFileTest1.class:true",
            "SourceFileTest2.class:true",
            "SourceFileTest3.class:true",
            "SourceFileTest4.class:true",
            "SourceFileTest5.class:false",
            "SourceFileTest6.class:false",
            "SourceFileTest7.class:false",
            "SourceFileTest8.class:false"
    }, delimiter = ':')
    void testUsesSkippyExtension(String fileName, boolean expectedValue) throws URISyntaxException {
        var classFile = Paths.get(getClass().getResource(fileName).toURI());
        assertEquals(expectedValue, new DecoratedClass(classFile).usesSkippyExtension());
    }
    @ParameterizedTest
    @CsvSource(value = {
            "SourceFileTest1.class:com.example.SourceFileTest1",
            "SourceFileTest2.class:com.example.SourceFileTest2"
    }, delimiter = ':')
    void testGetFullyQualifiedClassName(String fileName, String expectedValue) throws URISyntaxException {
        var classFile = Paths.get(getClass().getResource(fileName).toURI());
        assertEquals(expectedValue, new DecoratedClass(classFile).getFullyQualifiedClassName());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "SourceFileTest1.class:DM/flrfJfkathBz4vkrYkw==",
            "SourceFileTest2.class:J0uUoArClaJaIlkhCic6vg=="
    }, delimiter = ':')
    void getHash(String fileName, String expectedValue) throws URISyntaxException {
        var classFile = Paths.get(getClass().getResource(fileName).toURI());
        assertEquals(expectedValue, new DecoratedClass(classFile).getHash(mock(Logger.class)));
    }

}