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
import java.nio.file.Path;
import java.util.*;

import static java.util.Comparator.comparing;

/**
 * Collects {@link ClassFile}s in a project.
 *
 * @author Florian McKee
 */
public final class ClassFileCollector {

    private final Project project;
    private final SourceSetContainer sourceSetContainer;

    /**
     * C'tor
     *
     * @param project a {@link Project}
     * @param sourceSetContainer a {@link SourceSetContainer}
     */
    public ClassFileCollector(Project project, SourceSetContainer sourceSetContainer) {
        this.project = project;
        this.sourceSetContainer = sourceSetContainer;
    }

    /**
     * Collects all {@link ClassFile}s in the project.
     *
     * @return all {@link ClassFile}s in the project
     */
    public Map<Path, List<ClassFile>> collect() {
        var result = new HashMap<Path, List<ClassFile>>();
        for (var sourceSet : sourceSetContainer) {
            result.putAll(collect(sourceSet));
        }
        return result;
    }

    /**
     * Collects all {@link ClassFile}s in the output directories of the {@code sourceSet} organized by classes folders.
     *
     * @param sourceSet
     * @return all {@link ClassFile}s in the output directories of the {@code sourceSet} organized by classes folders
     */
     Map<Path, List<ClassFile>> collect(SourceSet sourceSet) {
        var classesDirs = sourceSet.getOutput().getClassesDirs().getFiles();
        var result = new HashMap<Path, List<ClassFile>>();
        for (var classesDir : classesDirs) {
            result.put(classesDir.toPath(), sort(collect(classesDir)));
        }
        return result;
    }

    private List<ClassFile> collect(File directory) {
        var result = new LinkedList<ClassFile>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    result.addAll(collect(file));
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