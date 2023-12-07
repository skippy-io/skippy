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

import io.skippy.core.asm.ClassNameExtractor;
import io.skippy.core.asm.DebugAgnosticHash;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

/**
 * Wrapper that adds a bunch of functionality on top of a class file that has been analyzed by Skippy.
 *
 * @author Florian McKee
 */
class ClassFile {

    private static final Logger LOGGER = LogManager.getLogger(ClassFile.class);

    private final Path classFile;
    private final String hash;

    /**
     * C'tor.
     *
     * @param classFile the class file in the file system (e.g., /user/johndoe/repos/demo/build/classes/java/main/com/example/Foo.class)
     * @param hash the MD5 hash of the content of the class file (e.g., YA9ExftvTDku3TUNsbkWIw==)
     */
    ClassFile(Path classFile, String hash) {
        this.classFile = classFile;
        this.hash = hash;
    }

    FullyQualifiedClassName getFullyQualifiedClassName() {
        return new FullyQualifiedClassName(ClassNameExtractor.getFullyQualifiedClassName(classFile));
    }

    /**
     * Returns {@code true} if the class file has changed since it was analyzed, {@code false} otherwise.
     *
     * @return {@code true} if the class file has changed since it was analyzed, {@code false} otherwise.
     */
    boolean hasChanged() {
        if ( ! classFile.toFile().exists()) {
            return true;
        }
        String newHash = DebugAgnosticHash.hash(classFile);
        if ( ! hash.equals(newHash)) {
            return true;
        }
        return false;
    }

}
