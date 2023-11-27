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

package io.skippy.core;

import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link AnalyzedFile}.
 *
 * @author Florian McKee
 */
public class AnalyzedFileTest {

    @Test
    void testSourceAndClassFileHaveNotChanged() throws URISyntaxException {
        var foo = new AnalyzedFile(
                new FullyQualifiedClassName("com.example.Foo"),
                Path.of(getClass().getResource("analyzedfile/Foo.java").toURI()),
                Path.of(getClass().getResource("analyzedfile/Foo.class").toURI()),
                "cGN5C7g/BdD4rxxFBgZ7pw==",
                "54Uq2W8MWDOi6dCDnWoLVQ=="
        );

        assertEquals(false, foo.sourceFileHasChanged());
        assertEquals(false, foo.classFileHasChanged());
    }

    @Test
    void testSourceFileHasChanged() throws URISyntaxException {
        var fooNew = new AnalyzedFile(
                new FullyQualifiedClassName("com.example.Foo"),
                Path.of(getClass().getResource("analyzedfile/Foo.java").toURI()),
                Path.of(getClass().getResource("analyzedfile/Foo.class").toURI()),
                "NEW-SOURCE-FILE-HASH",
                "54Uq2W8MWDOi6dCDnWoLVQ=="
        );
        assertEquals(true, fooNew.sourceFileHasChanged());
        assertEquals(false, fooNew.classFileHasChanged());
    }

    @Test
    void testClassFileHasChanged() throws URISyntaxException {
        var fooNew = new AnalyzedFile(
                new FullyQualifiedClassName("com.example.Foo"),
                Path.of(getClass().getResource("analyzedfile/Foo.java").toURI()),
                Path.of(getClass().getResource("analyzedfile/Foo.class").toURI()),
                "cGN5C7g/BdD4rxxFBgZ7pw==",
                "NEW-CLASS-FILE-HASH"
        );
        assertEquals(false, fooNew.sourceFileHasChanged());
        assertEquals(true, fooNew.classFileHasChanged());
    }

    @Test
    void testSourceAndClassFileHaveChanged() throws URISyntaxException {
        var fooNew = new AnalyzedFile(
                new FullyQualifiedClassName("com.example.Foo"),
                Path.of(getClass().getResource("analyzedfile/Foo.java").toURI()),
                Path.of(getClass().getResource("analyzedfile/Foo.class").toURI()),
                "NEW-SOURCE-FILE-HASH",
                "NEW-CLASS-FILE-HASH"
        );
        assertEquals(true, fooNew.sourceFileHasChanged());
        assertEquals(true, fooNew.classFileHasChanged());
    }

}