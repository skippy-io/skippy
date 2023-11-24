package io.skippy.gradle.core;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SourceFileTest {

    @ParameterizedTest
    @CsvSource(value = {
            "SourceFileTest1.java:true",
            "SourceFileTest2.java:true",
            "SourceFileTest3.java:true",
            "SourceFileTest4.java:true",
            "SourceFileTest5.java:false",
            "SourceFileTest6.java:false",
            "SourceFileTest7.java:false",
            "SourceFileTest8.java:false"
    }, delimiter = ':')
    void testUsesSkippyExtension(String fileName, boolean expectedValue) throws URISyntaxException {
        var sourceFile = Paths.get(getClass().getResource(fileName).toURI());
        var resourcesFolder = getResourceFolder(fileName);
        assertEquals(expectedValue, SourceFile.of(sourceFile, resourcesFolder, resourcesFolder).usesSkippyExtension());
    }
    @ParameterizedTest
    @CsvSource(value = {
            "SourceFileTest1.java:io.skippy.gradle.core.SourceFileTest1",
            "SourceFileTest2.java:io.skippy.gradle.core.SourceFileTest2"
    }, delimiter = ':')
    void testGetFullyQualifiedClassName(String fileName, String expectedValue) throws URISyntaxException {
        var sourceFile = Paths.get(getClass().getResource(fileName).toURI());
        var resourcesFolder = getResourceFolder(fileName);
        assertEquals(expectedValue, SourceFile.of(sourceFile, resourcesFolder, resourcesFolder).getFullyQualifiedClassName());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "SourceFileTest1.java:83HqrtEJmFOXaYdNhP5xPw==",
            "SourceFileTest2.java:KYSi6wFB30NmABEN9r6J1g=="
    }, delimiter = ':')
    void getSourceFileHash(String fileName, String expectedValue) throws URISyntaxException {
        var sourceFile = Paths.get(getClass().getResource(fileName).toURI());
        var resourcesFolder = getResourceFolder(fileName);
        assertEquals(expectedValue, SourceFile.of(sourceFile, resourcesFolder, resourcesFolder).getSourceFileHash());
    }
    @ParameterizedTest
    @CsvSource(value = {
            "SourceFileTest1.java:OQICr5FuS6ZnV5lL4f/GGQ==",
            "SourceFileTest2.java:YA9ExftvTDku3TUNsbkWIw=="
    }, delimiter = ':')
    void getClassFileHash(String fileName, String expectedValue) throws URISyntaxException {
        var sourceFile = Paths.get(getClass().getResource(fileName).toURI());
        var resourcesFolder = getResourceFolder(fileName);
        assertEquals(expectedValue, SourceFile.of(sourceFile, resourcesFolder, resourcesFolder).getClassFileHash());
    }

    private Path getResourceFolder(String filename) throws URISyntaxException {
        var sourceDirectory = Paths.get(getClass().getResource(filename).toURI());
        // remove filename
        sourceDirectory = sourceDirectory.getParent();

        var packageName = this.getClass().getPackageName();
        while (packageName.contains(".")) {
            packageName = packageName.substring(0, packageName.lastIndexOf("."));
            sourceDirectory = sourceDirectory.getParent();
        }
        if ( ! packageName.isEmpty()) {
            sourceDirectory = sourceDirectory.getParent();
        }
        return sourceDirectory;
    }

}
