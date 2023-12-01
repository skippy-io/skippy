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
import io.skippy.gradle.asm.DebugAgnosticHash;
import io.skippy.gradle.asm.SkippyJUnit5Detector;
import io.skippy.gradle.util.Projects;
import io.skippy.gradle.util.Tasks;
import org.gradle.api.Project;
import org.gradle.api.tasks.testing.Test;

import java.nio.file.Path;
import java.util.List;

import static java.util.Comparator.comparing;

/**
 * Wrapper that adds a bunch of functionality on top of a class file.
 *
 * @author Florian McKee
 */
final class DecoratedClass {

    private final Project project;
    private final Path classFile;

    /**
     * C'tor.
     *
     * @param project the Gradle {@link Project}
     * @param classFile the class file in the file system (e.g., /user/johndoe/repos/demo/build/classes/java/main/com/example/Foo.class)
     */
    DecoratedClass(Project project, Path classFile) {
        this.project = project;
        this.classFile = classFile;
    }

    static List<DecoratedClass> fromAllClassesIn(Project project) {
        var classFiles = Projects.findAllClassFiles(project);
        return classFiles.stream()
                .map(classFile -> new DecoratedClass(project, classFile))
                .sorted(comparing(DecoratedClass::getFullyQualifiedClassName))
                .toList();
    }

    static List<DecoratedClass> fromAllSkippifiedTestsIn(Project project) {
        return fromAllClassesIn(project).stream()
                .filter(clazz -> SkippyJUnit5Detector.usesSkippyExtension(clazz.getAbsolutePath()))
                .toList();
    }

    /**
     * Returns the fully qualified class name (e.g., com.example.Foo).
     *
     * @return the fully qualified class name (e.g., com.example.Foo)
     */
    String getFullyQualifiedClassName() {
        return ClassNameExtractor.getFullyQualifiedClassName(classFile);
    }


    /**
     * Returns the absolute {@link Path} of the class file.
     *
     * @return the absolute {@link Path} of the class file.
     */
    Path getAbsolutePath() {
        return classFile;
    }

    /**
     * Returns the relative {@link Path} of the class file relative to the project root,
     * (e.g., src/main/java/com/example/Foo.java)
     *
     * @return the relative {@link Path} of the class file relative to the project root,
     *      (e.g., src/main/java/com/example/Foo.java)
     */
    Path getRelativePath() {
        return project.getProjectDir().toPath().relativize(classFile);
    }

    /**
     * Returns a hash of the class file in BASE64 encoding.
     *
     * @return a hash of the class file in BASE64 encoding
     */
    String getHash() {
        return DebugAgnosticHash.hash(classFile);
    }

    /**
     * Returns {@code true} if this class is the test that uses the Skippy extension, {@code false} otherwise.
     *
     * @return {@code true} if this class is the test that uses the Skippy extension, {@code false} otherwise
     */
    boolean usesSkippyExtension() {
        return SkippyJUnit5Detector.usesSkippyExtension(classFile);
    }

    /**
     * Returns the {@link Test} task that runs this class (assuming it is a test).
     *
     * @return the {@link Test} task that runs this class (assuming it is a test)
     */
    Test getTestTask() {
        if ( ! usesSkippyExtension()) {
            throw new UnsupportedOperationException("The testTask property is only available for skippified tests.");
        }
        return Tasks.getTestTaskFor(project, classFile);
    }

}
