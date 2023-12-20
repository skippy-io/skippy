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

package io.skippy.gradle.model;

import io.skippy.gradle.asm.ClassNameExtractor;
import io.skippy.gradle.asm.DebugAgnosticHash;
import org.gradle.api.Project;

import java.nio.file.Path;

/**
 * Thin wrapper around a class file in a {@link Project} that adds a couple of convenience methods.
 *
 * @author Florian McKee
 */
public class ClassFile {

    private final Path classFile;
    private final String fullyQualifiedClassName;

    /**
     * C'tor.
     *
     * @param classFile the class file in the file system (e.g., /user/johndoe/repos/demo/build/classes/java/main/com/example/Foo.class)
     */
    public ClassFile(Path classFile) {
        this.classFile = classFile;
        this.fullyQualifiedClassName = ClassNameExtractor.getFullyQualifiedClassName(classFile);
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
     * Returns the fully qualified class name (e.g., c.e.Foo).
     *
     * @return the fully qualified class name (e.g., c.e.Foo)
     */
    public String getShortClassName() {
        var result = "";
        String[] split = fullyQualifiedClassName.split("\\.");
        for (int i = 0; i < split.length; i++) {
            if (i < split.length - 1) {
                result += split[i].charAt(0) + ".";
            } else {
                result += split[i];
            }
        }
        return result;
    }

    /**
     * Returns the absolute {@link Path} of the class file.
     *
     * @return the absolute {@link Path} of the class file.
     */
    public Path getAbsolutePath() {
        return classFile;
    }

    /**
     * Returns a hash of the contents of the class file.
     *
     * @return a hash of the contents of the class file
     */
    public String getHash() {
        return DebugAgnosticHash.hash(classFile);
    }

}
