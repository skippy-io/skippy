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

import io.skippy.build.SkippyBuildApi;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.SourceSetContainer;

import java.util.Optional;

/**
 * Creates a {@link SkippyBuildApi} for a {@link Project} or {@link Task}.
 *
 * @author Florian McKee
 */
final class SkippyBuildApiFactory {

    static Optional<SkippyBuildApi> getInstanceFor(Project project) {
        var sourceSetContainer = project.getExtensions().findByType(SourceSetContainer.class);
        if (sourceSetContainer == null) {
            return Optional.empty();
        }
        return Optional.of(new SkippyBuildApi(project.getProjectDir().toPath(),
                new GradleClassFileCollector(sourceSetContainer)));
    }

    static Optional<SkippyBuildApi> getInstanceFor(Task task) {
        var maybeBuildApi = getInstanceFor(task.getProject());
        if (maybeBuildApi.isEmpty()) {
            task.getLogger().warn(("No SourceSetContainer found: %s will be skipped. " +
                    "Did you forget to apply the 'java' or 'java-library' plugin?").formatted(task.getName()));
        }
        return maybeBuildApi;
    }

}
