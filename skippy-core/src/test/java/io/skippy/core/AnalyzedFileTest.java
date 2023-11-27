package io.skippy.core;

import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

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