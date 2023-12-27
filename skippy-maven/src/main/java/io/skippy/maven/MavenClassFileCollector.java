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

package io.skippy.maven;

import io.skippy.build.ClassFile;
import io.skippy.build.ClassFileCollector;
import io.skippy.build.DirectoryWithClassFiles;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;

/**
 * Collects {@link ClassFile}s in a project.
 *
 * @author Florian McKee
 */
final class MavenClassFileCollector implements ClassFileCollector {

    private final MavenProject project;

    MavenClassFileCollector(MavenProject project) {
        this.project = project;
    }

    /**
     * Collects all {@link ClassFile}s in the output directories of the project organized by classes folders.
     *
     * @return all {@link ClassFile}s in the output directories of the project organized by classes folders.
     */
    @Override
    public List<DirectoryWithClassFiles> collect() {
        var classesDir = new File(project.getBuild().getOutputDirectory());
        var testClassesDir = new File(project.getBuild().getTestOutputDirectory());
        return asList(
                new DirectoryWithClassFiles(classesDir.toPath(), sort(collect(classesDir))),
                new DirectoryWithClassFiles(testClassesDir.toPath(), sort(collect(testClassesDir)))
        );
    }

    private List<ClassFile> collect(File directory) {
        var result = new LinkedList<ClassFile>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    result.addAll(collect(file));
                } else if (file.getName().endsWith(".class")) {
                    result.add(new ClassFile(file.toPath()));
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