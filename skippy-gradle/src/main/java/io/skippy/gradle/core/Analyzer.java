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
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.util.Comparator.comparing;

/**
 * Analyzes all sources in a project.
 *
 * @author Florian McKee
 */
public class Analyzer {

    /**
     * Analyzes all sources in a project.
     *
     * @param project a {@link Project}
     * @return a list of {@link AnalyzedFile}s
     */
    public static List<AnalyzedFile> analyzeProject(Project project) {
        var result = new ArrayList<AnalyzedFile>();
        var sourceSetContainer = project.getExtensions().getByType(SourceSetContainer.class);
        for (SourceSet sourceSet : sourceSetContainer) {
            result.addAll(analyzeSourceSet(sourceSet));
        }
        return result.stream().sorted(comparing(AnalyzedFile::getFullyQualifiedClassName)).toList();
    }

    private static List<AnalyzedFile> analyzeSourceSet(SourceSet sourceSet) {
        var javaFiles = sourceSet.getJava().getFiles().stream()
                .map(File::toPath)
                .toList();
        var sourceDirectories = sourceSet.getJava().getSrcDirs().stream()
                .map(File::toPath)
                .toList();

        var result = new ArrayList<AnalyzedFile>();
        for (var javaFile : javaFiles) {
            for (var sourceDirectory : sourceDirectories) {
                if (javaFile.toAbsolutePath().startsWith(sourceDirectory.toAbsolutePath())) {
                    result.add(AnalyzedFile.of(javaFile, sourceDirectory, sourceSet.getJava().getClassesDirectory().get().getAsFile().toPath()));
                    break;
                }
            }
        }
        return result;
    }
}
