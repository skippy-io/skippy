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
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.util.stream.Stream;

/**
 * Returns the destination directories of the {@link JavaCompile} task.
 *
 * @author Eugeniu Tufar
 * @author Florian McKee
 */
final class AndroidDestinationDirectoryCollector {
    private AndroidDestinationDirectoryCollector() {}

    private static Stream<File> collect(Project project) {
        return project.getTasks()
                .withType(JavaCompile.class)
                .stream()
                .map(task -> task.getDestinationDirectory().getAsFile().get());
    }

    static Stream<File> collectIfExists(Project project) {
        return collect(project)
                .filter(File::exists);
    }
}
