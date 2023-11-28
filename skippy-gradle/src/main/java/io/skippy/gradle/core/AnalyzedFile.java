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

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.regex.Pattern.quote;


/**
 * A file that has been analyzed by Skippy:
 * <ul>
 *     <li>a fully-qualified class name</li>
 *     <li>a path to the source file</li>
 *     <li>a path to the class file</li>
 * </ul>
 *
 * TODO: Ideally, the corresponding Class object should be stored as well to detect the Skippy extension and to infer the
 * fully qualified class name.
 *
 * @author Florian McKee
 */
public class AnalyzedFile {

    private static final List<String> SKIPPY_EXTENSION_PATTERNS = asList(
            "@ExtendWith(Skippy.class)",
            "@ExtendWith(io.skippy.junit5.Skippy.class)",
            "@org.junit.jupiter.api.extension.ExtendWith(Skippy.class)",
            "@org.junit.jupiter.api.extension.ExtendWith(io.skippy.junit5.Skippy.class)"
    );

    private static final Function<String,String> REMOVE_WHITESPACES = s -> s.replaceAll("\\s", "");

    private final String fullyQualifiedClassName;
    private final Path sourceFile;
    private final Path classFile;

    /**
     * C'tor.
     *
     * @param fullyQualifiedClassName the fully-qualified class name (e.g., com.example.Foo)
     * @param sourceFile the source file in the file system (e.g., /User/johndoe/repo/src/main/java/com/example/Foo.java)
     * @param classFile the class file in the file system (e.g., /user/johndoe/repos/demo/build/classes/java/main/com/example/Foo.class)
     */
    private AnalyzedFile(String fullyQualifiedClassName, Path sourceFile, Path classFile) {
        this.fullyQualifiedClassName = fullyQualifiedClassName;
        this.sourceFile = sourceFile;
        this.classFile = classFile;
    }

    /**
     * Factory method that constructs a {@link AnalyzedFile} based of a {@param sourceFile}, {@param sourceFolder} and
     * {@param classesFolder}.
     *
     * @param sourceFile the source file in the file system (e.g., /User/johndoe/repo/src/main/java/com/example/Foo.java)
     * @param sourceFolder the source folder in the file system (e.g., /User/johndoe/repo/src/main/java)
     * @param classesFolder the classes file in the file system (e.g., /user/johndoe/repos/demo/build/classes/java/main)
     *
     * @return an {@link AnalyzedFile}
     */
    public static AnalyzedFile of(Path sourceFile, Path sourceFolder, Path classesFolder) {
        var pathWithExtension = sourceFolder.relativize(sourceFile.toAbsolutePath()).toString();
        var fullyQualifiedClassName = pathWithExtension
                .substring(0, pathWithExtension.lastIndexOf("."))
                .replaceAll("/", ".");
        var classFile = classesFolder.resolve(Path.of(fullyQualifiedClassName.replaceAll(quote("."), "/") + ".class"));
        return new AnalyzedFile(fullyQualifiedClassName, sourceFile, classFile);
    }

    /**
     * Returns the fully qualified class name (e.g., com.example.Foo).
     *
     * @return the fully qualified class name (e.g., com.example.Foo)
     */
    public String getFullyQualifiedClassName() {
        return fullyQualifiedClassName;
    }

    /**
     * Returns the filename of the source file relative to the {@param projectDirectory},
     * (e.g., src/main/java/com/example/Foo.java)
     *
     * @return the filename of the source file relative to the {@param projectDirectory},
     *      (e.g., src/main/java/com/example/Foo.java)
     */
    public String getSourceFileName(Path projectDir) {
        return projectDir.relativize(sourceFile).toString();
    }

    /**
     * Returns the filename of the class file relative to the {@param projectDirectory},
     * (e.g., src/main/java/com/example/Foo.java)
     *
     * @return the filename of the class file relative to the {@param projectDirectory},
     *      (e.g., src/main/java/com/example/Foo.java)
     */

    public String getClassFileName(Path projectDir) {
        return projectDir.relativize(classFile).toString();
    }

    /**
     * Returns the MD5 hash of the Java source file in BASE64 encoding.
     *
     * @param logger the Gradle logger
     * @return the MD5 hash of the Java source file in BASE64 encoding
     */
    public String getSourceFileHash(Logger logger) {
        return getHash(sourceFile, logger);
    }

    /**
     * Returns the MD5 hash of the Java class file in BASE64 encoding.
     *
     * @param logger the Gradle logger
     * @return the MD5 hash of the Java class file in BASE64 encoding
     */
    public String getClassFileHash(Logger logger) {
        return getHash(classFile, logger);
    }

    /**
     * Returns {@code true} if this class is the test that uses the Skippy extension, {@code false} otherwise.
     *
     * @return {@code true} if this class is the test that uses the Skippy extension, {@code false} otherwise
     */
    public boolean usesSkippyExtension() {
        try {
            var fileContent = Files.readAllLines(sourceFile).stream()
                    .map(REMOVE_WHITESPACES)
                    .toList();
            for (var pattern : SKIPPY_EXTENSION_PATTERNS) {
                for (var line : fileContent) {
                    if (line.startsWith(pattern)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getHash(Path file, Logger logger) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = Files.readAllBytes(file);
            md.update(bytes);
            var hash = Base64.getEncoder().encodeToString(md.digest());

            if (logger.isInfoEnabled()) {
                logger.info("Generating hash for file %s:".formatted(file.getFileName()));
                logger.info("  content=%s".formatted(Base64.getEncoder().encodeToString(bytes)));
                logger.info("  hash=%s".formatted(hash));
            }

            return hash;
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
    }


}
