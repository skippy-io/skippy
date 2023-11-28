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

package io.skippy.gradle;

import io.skippy.gradle.asm.ClassNameExtractor;
import io.skippy.gradle.asm.SkippyJUnit5Detector;
import org.gradle.api.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


/**
 * Wrapper that adds a bunch of functionality on top of a class file.
 *
 * @author Florian McKee
 */
public final class DecoratedClass {

    private final Path classFile;

    /**
     * C'tor.
     *
     * @param classFile the class file in the file system (e.g., /user/johndoe/repos/demo/build/classes/java/main/com/example/Foo.class)
     */
    DecoratedClass(Path classFile) {
        this.classFile = classFile;
    }

    /**
     * Returns the fully qualified class name (e.g., com.example.Foo).
     *
     * @return the fully qualified class name (e.g., com.example.Foo)
     */
    public String getFullyQualifiedClassName() {
        return ClassNameExtractor.getFullyQualifiedClassName(classFile);
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
     * Returns a MD5 hash in BASE64 encoding.
     *
     * @param logger the Gradle logger
     * @return a MD5 hash in BASE64 encoding
     */
    public String getHash(Logger logger) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = Files.readAllBytes(classFile);
            md.update(bytes);
            var hash = Base64.getEncoder().encodeToString(md.digest());

            if (logger.isInfoEnabled()) {
                logger.info("Generating hash for file %s:".formatted(classFile.getFileName()));
                logger.info("  content=%s".formatted(Base64.getEncoder().encodeToString(bytes)));
                logger.info("  hash=%s".formatted(hash));
            }

            return hash;
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns {@code true} if this class is the test that uses the Skippy extension, {@code false} otherwise.
     *
     * @return {@code true} if this class is the test that uses the Skippy extension, {@code false} otherwise
     */
    public boolean usesSkippyExtension() {
        return SkippyJUnit5Detector.usesSkippyExtension(classFile);
    }


}
