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

package io.skippy.gradle.collector;

import io.skippy.gradle.model.ClassFile;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.util.Comparator.comparing;

/**
 * Collects all {@link ClassFile}s in {@link Project}s and {@link SourceSet}s.
 */
public final class ClassFileCollector {

    /**
     * Collects all {@link ClassFile}s across all output directories of the {@param project}.
     *
     * @return all {@link ClassFile}s across all output directories of the {@param project}
     */
    public List<ClassFile> collectAllInProject(Project project) {
        var result = new ArrayList<ClassFile>();
        var sourceSetContainer = project.getExtensions().getByType(SourceSetContainer.class);
        for (var sourceSet : sourceSetContainer) {
            result.addAll(collectAllInSourceSet(project, sourceSet));
        }
        return sort(result);
    }

    /**
     * Collects all {@link ClassFile}s in the output directories of the {@param sourceSet}.
     *
     * @return all {@link ClassFile}s in the output directories of the {@param sourceSet}
     */
    List<ClassFile> collectAllInSourceSet(Project project, SourceSet sourceSet) {
        var classesDirs = sourceSet.getOutput().getClassesDirs().getFiles();
        var result = new ArrayList<ClassFile>();
        for (var classesDir : classesDirs) {
            result.addAll(collectAllInDirectory(project, classesDir));
        }
        return sort(result);
    }

    private static List<ClassFile> collectAllInDirectory(Project project, File directory) {
        var result = new LinkedList<ClassFile>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    result.addAll(collectAllInDirectory(project, file));
                } else if (file.getName().endsWith(".class")) {
                    result.add(new ClassFile(project, file.toPath()));
                }
            }
        }
        return result;
    }

    private List<ClassFile> sort(List<ClassFile> input) {
        return input.stream()
                .sorted(comparing(ClassFile::getFullyQualifiedClassName))
                .toList();
    }

}