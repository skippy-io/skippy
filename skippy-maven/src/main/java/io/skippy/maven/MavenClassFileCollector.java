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

package io.skippy.maven;

import io.skippy.common.model.ClassFile;
import io.skippy.build.ClassFileCollector;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.util.Comparator.comparing;

/**
 * Collects {@link ClassFile}s in the output directories of the  project.
 *
 * @author Florian McKee
 */
final class MavenClassFileCollector implements ClassFileCollector {

    private final MavenProject project;

    MavenClassFileCollector(MavenProject project) {
        this.project = project;
    }

    /**
     * Collects all {@link ClassFile}s in the output directories of the project.
     *
     * @return all {@link ClassFile}s in the output directories of the project.
     */
    @Override
    public List<ClassFile> collect() {
        var classesDir = new File(project.getBuild().getOutputDirectory());
        var testClassesDir = new File(project.getBuild().getTestOutputDirectory());
        var result = new ArrayList<ClassFile>();
        result.addAll(sort(collect(classesDir, classesDir)));
        result.addAll(sort(collect(testClassesDir, testClassesDir)));
        return result;
    }

    private List<ClassFile> collect(File outputFolder, File searchDirectory) {
        var result = new LinkedList<ClassFile>();
        File[] files = searchDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    result.addAll(collect(outputFolder, file));
                } else if (file.getName().endsWith(".class")) {
                    result.add(ClassFile.fromFileSystem(project.getBasedir().toPath(), outputFolder.toPath(), file.toPath()));
                }
            }
        }
        return result;
    }

    private List<ClassFile> sort(List<ClassFile> input) {
        return input.stream()
                .sorted(comparing(ClassFile::getClazz))
                .toList();
    }

}