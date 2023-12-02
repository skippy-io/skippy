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

package io.skippy.gradle.util;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;
import java.nio.file.Path;

import static io.skippy.gradle.Profiler.profile;

/**
 * Utility methods that return / operate on to Gradle {@link SourceSet}s.
 *
 * @author Florian McKee
 */
final class SourceSets {

    /**
     * Finds the {@link SourceSet} in the given {@code project} that contains the {@code classFile} in one of its
     * output directories.
     *
     * <br /><br />
     *
     * Let's assume you have the <b>test</b> source set in <code>src/test</code> and another, custom source set
     * <b>intTest</b> for integration tests that is declared as follows:
     * <pre>
     * sourceSets {
     *     intTest {
     *         ...
     *     }
     * }
     * </pre>
     *
     * This method will
     * <ul>
     *  <li>return <b>test</b> if the class is located in build/classes/java/test</li>
     *  <li>return <b>intTest</b> if the class is located in build/classes/java/intTest</li>
     * </ul>
     *
     * @param project the Gradle {@link Project}
     * @param classFile a class file
     * @return the {@link SourceSet} in the given {@code project} that contains the {@code classFile} in one of its
     *      output directories
     */
    static SourceSet findSourceSetContaining(Project project, Path classFile) {
        return profile(SourceSets.class, "findSourceSetContaining", () -> {
            SourceSetContainer sourceSetContainer = project.getExtensions().getByType(SourceSetContainer.class);
            for (SourceSet sourceSet : sourceSetContainer) {
                FileCollection classesDirs = sourceSet.getOutput().getClassesDirs();
                for (File classesDir : classesDirs.getFiles()) {
                    if (classFile.startsWith(classesDir.toPath())) {
                        return sourceSet;
                    }
                }
            }
            throw new RuntimeException("Unable to determine SourceSet for class '%s'.".formatted(classFile));
        });
    }

}