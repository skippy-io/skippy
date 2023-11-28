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

import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;
import java.util.*;

import static java.util.Comparator.comparing;

/**
 * Collects all class files in a project.
 *
 * @author Florian McKee
 */
public final class ClassCollector {

    /**
     * Collects all class files in the {@param project}.
     *
     * @param project a {@link Project}
     * @return a list of {@link DecoratedClass}s
     */
    public static List<DecoratedClass> collect(Project project) {
        var result = new ArrayList<DecoratedClass>();
        var sourceSetContainer = project.getExtensions().getByType(SourceSetContainer.class);
        for (SourceSet sourceSet : sourceSetContainer) {
            result.addAll(collect(sourceSet));
        }
        return result.stream().sorted(comparing(DecoratedClass::getFullyQualifiedClassName)).toList();
    }

    private static List<DecoratedClass> collect(SourceSet sourceSet) {
        var classesDirs = sourceSet.getOutput().getClassesDirs().getFiles();
        List<File> classes = classesDirs.stream().flatMap(dir -> getAllClasses(dir).stream()).toList();
        return classes.stream().map(classFile -> new DecoratedClass(classFile.toPath())).toList();
    }

    private static List<File> getAllClasses(File dir) {
        var result = new LinkedList<File>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    result.addAll(getAllClasses(file));
                } else if (file.getName().endsWith(".class")) {
                    result.add(file);
                }
            }
        }
        return result;
    }

}
