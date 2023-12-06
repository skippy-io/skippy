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
 * Thin wrapper around a class file that adds a couple of convenience methods.
 *
 * @author Florian McKee
 */
public final class ClassFile {

    private final Project project;
    private final Path classFile;

    /**
     * C'tor.
     *
     * @param project the Gradle {@link Project}
     * @param classFile the class file in the file system (e.g., /user/johndoe/repos/demo/build/classes/java/main/com/example/Foo.class)
     */
    public ClassFile(Project project, Path classFile) {
        this.project = project;
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
     * Returns the absolute {@link Path} of the class file.
     *
     * @return the absolute {@link Path} of the class file.
     */
    public Path getAbsolutePath() {
        return classFile;
    }

    /**
     * Returns the relative {@link Path} of the class file relative to the project root,
     * (e.g., src/main/java/com/example/Foo.java)
     *
     * @return the relative {@link Path} of the class file relative to the project root,
     *      (e.g., src/main/java/com/example/Foo.java)
     */
    public Path getRelativePath() {
        return project.getProjectDir().toPath().relativize(classFile);
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
