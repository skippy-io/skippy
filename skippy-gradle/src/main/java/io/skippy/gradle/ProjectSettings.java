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

package io.skippy.gradle;

import io.skippy.core.SkippyBuildApi;
import io.skippy.core.SkippyRepository;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A sub-set of relevant {@link Project} properties that are compatible with Gradle's Configuration Cache.
 *
 * @author Florian McKee
 */
class ProjectSettings {

    final boolean buildSupportsSkippy;
    final List<File> classesDirs;
    final SkippyPluginExtension skippyPluginExtension;
    final Path projectDir;
    final Path buildDir;

    /**
     * C'tor.
     *
     * @param projectSupportsSkippy {@code true} if the build supports Skippy, {@code false} otherwise
     * @param classesDirs the folders that contain compiled class files
     * @param skippyExtension the {@link SkippyPluginExtension}
     * @param projectDir the project directory (e.g., /repos/my-project)
     * @param buildDir the build directory (e.g., /repos/my-project/build)
     */
    private ProjectSettings(boolean projectSupportsSkippy, List<File> classesDirs, SkippyPluginExtension skippyExtension, Path projectDir, Path buildDir) {
        this.buildSupportsSkippy = projectSupportsSkippy;
        this.classesDirs = classesDirs;
        this.skippyPluginExtension = skippyExtension;
        this.projectDir = projectDir;
        this.buildDir = buildDir;
    }

    static ProjectSettings from(Project project) {
        var sourceSetContainer = project.getExtensions().findByType(SourceSetContainer.class);
        if (sourceSetContainer != null) {
            // new ArrayList<>() is a workaround for https://github.com/gradle/gradle/issues/26942
            var classesDirs = new ArrayList<>(sourceSetContainer.stream().flatMap(sourceSet -> sourceSet.getOutput().getClassesDirs().getFiles().stream()).toList());
            var skippyExtension = project.getExtensions().getByType(SkippyPluginExtension.class);
            var projectDir = project.getProjectDir().toPath();
            var buildDir = project.getLayout().getBuildDirectory().getAsFile().get().toPath();
            return new ProjectSettings(sourceSetContainer != null, classesDirs, skippyExtension, projectDir, buildDir);
        } else {
            return new ProjectSettings(false, null, null, null, null);
        }
    }

    void ifBuildSupportsSkippy(Consumer<SkippyBuildApi> action) {
        if (buildSupportsSkippy) {
            var skippyConfiguration = skippyPluginExtension.toSkippyConfiguration();
            var skippyBuildApi = new SkippyBuildApi(
                    skippyConfiguration,
                    new GradleClassFileCollector(projectDir, classesDirs),
                    SkippyRepository.getInstance(
                            skippyConfiguration,
                            projectDir,
                            buildDir
                    )
            );
            action.accept(skippyBuildApi);
        }
    }

}