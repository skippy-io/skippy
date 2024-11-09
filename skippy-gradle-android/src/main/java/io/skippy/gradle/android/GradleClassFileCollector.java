/*
 * Copyright 2023-2024 the original author or authors.
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

package io.skippy.gradle.android;

import io.skippy.core.ClassFileCollector;
import io.skippy.core.ClassFile;
import io.skippy.core.Profiler;
import org.gradle.api.tasks.SourceSet;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * Collects {@link ClassFile}s across all {@link SourceSet}s in a project.
 *
 * @author Florian McKee
 */
final class GradleClassFileCollector implements ClassFileCollector {

    private final Path projectDir;
    private final List<File> classesDirs;

    GradleClassFileCollector(Path projectDir, List<File> classesDirs) {
        this.projectDir = projectDir;
        this.classesDirs = classesDirs;
    }

    /**
     * Collects all {@link ClassFile}s in the output directories of the project organized by classes folders.
     *
     * @return all {@link ClassFile}s in the output directories of the project organized by classes folders.
     */
    @Override
    public List<ClassFile> collect() {
        return Profiler.profile("GradleClassFileCollector#collect", () -> {
            var result = new ArrayList<ClassFile>();
            for (var classesDir : classesDirs) {
                result.addAll(sort(collect(classesDir, classesDir)));
            }
            return result;
        });
    }

    private List<ClassFile> collect(File outputFolder, File directory) {
        var result = new LinkedList<ClassFile>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    result.addAll(collect(outputFolder, file));
                } else if (file.getName().endsWith(".class")) {
                    result.add(ClassFile.fromFileSystem(projectDir, outputFolder.toPath(), file.toPath()));
                }
            }
        }
        return result;
    }

    private List<ClassFile> sort(List<ClassFile> input) {
        return input.stream()
                .sorted()
                .toList();
    }

}