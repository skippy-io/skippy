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
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility methods that return / operate on to Gradle {@link org.gradle.api.Project}s.
 *
 * @author Florian McKee
 */
public final class Projects {

    /**
     * Collects all class files in the {@code project}.
     *
     * @param project a {@link Project}
     * @return a list of class files
     */
    public static List<Path> findAllClassFiles(Project project) {
        var result = new ArrayList<Path>();
        var sourceSetContainer = project.getExtensions().getByType(SourceSetContainer.class);
        for (var sourceSet : sourceSetContainer) {
            result.addAll(findAllClassFilesInSourceSet(sourceSet));
        }
        return result;
    }

    private static List<Path> findAllClassFilesInSourceSet(SourceSet sourceSet) {
        var classesDirs = sourceSet.getOutput().getClassesDirs().getFiles();
        var result = new ArrayList<Path>();
        for (var classesDir : classesDirs) {
            result.addAll(findAllClassFilesInDirectory(classesDir));
        }
        return result;
    }

    private static List<Path> findAllClassFilesInDirectory(File directory) {
        var result = new LinkedList<Path>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    result.addAll(findAllClassFilesInDirectory(file));
                } else if (file.getName().endsWith(".class")) {
                    result.add(file.toPath());
                }
            }
        }
        return result;
    }

}