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

import io.skippy.build.SkippyBuildApi;
import io.skippy.common.repository.SkippyRepository;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;


final class SkippyGradleUtils {

    static boolean supportsSkippy(Project project) {
        return project.getExtensions().findByType(SourceSetContainer.class) != null;
    }

    static SkippyBuildApi skippyBuildApi(Project project) {
        var sourceSetContainer = project.getExtensions().findByType(SourceSetContainer.class);
        var projectDir = project.getProjectDir().toPath();
        return new SkippyBuildApi(
                new GradleClassFileCollector(projectDir, sourceSetContainer),
                SkippyRepository.getInstance(projectDir)
        );
    }

}
