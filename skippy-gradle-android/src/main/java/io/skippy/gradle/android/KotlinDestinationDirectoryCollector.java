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

import org.gradle.api.Project;
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileTool;

import java.io.File;
import java.util.stream.Stream;

/**
 * Returns the destination directories of the {@link KotlinCompileTool} task.
 *
 * @author Eugeniu Tufar
 * @author Florian McKee
 */
final class KotlinDestinationDirectoryCollector {
    private KotlinDestinationDirectoryCollector() {}

    private static Stream<File> collect(Project project) {
        return project.getTasks().stream()
                .filter(task -> task.getName().startsWith("compile") && task.getName().endsWith("Kotlin"))
                .filter(task -> task instanceof KotlinCompileTool)
                .map(task -> (KotlinCompileTool) task)
                .map(kotlinCompileTool -> kotlinCompileTool.getDestinationDirectory().get().getAsFile());
    }

    static Stream<File> collectIfExists(Project project) {
        return collect(project)
                .filter(File::exists);
    }
}
