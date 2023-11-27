package io.skippy.core;

import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnalyzedFileListTest {

    @Test
    void testParse() throws URISyntaxException {
        var analyzedFilesTxt = Path.of(getClass().getResource("analyzedfiles/analyzedFiles.txt").toURI());
        var analyzedFiles = AnalyzedFileList.parse(analyzedFilesTxt);

        assertEquals(asList(
            new FullyQualifiedClassName("com.example.Foo"),
            new FullyQualifiedClassName("com.example.FooTest")
        ), analyzedFiles.getClasses());
    }

}