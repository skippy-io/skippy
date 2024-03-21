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

import io.skippy.core.SkippyApi;
import io.skippy.core.SkippyRepository;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;

import java.util.function.Consumer;

final class SkippyGradleUtils {

    static void ifBuildSupportsSkippy(Project project, Consumer<SkippyApi> action) {
        var sourceSetContainer = project.getExtensions().findByType(SourceSetContainer.class);
        if (sourceSetContainer != null) {
            var skippyExtension = project.getExtensions().getByType(SkippyPluginExtension.class);
            var skippyConfiguration = skippyExtension.toSkippyConfiguration();
            var projectDir = project.getProjectDir().toPath();
            var skippyBuildApi = new SkippyApi(
                    skippyConfiguration,
                    new GradleClassFileCollector(projectDir, sourceSetContainer),
                    SkippyRepository.getInstance(skippyConfiguration, projectDir)
            );
            action.accept(skippyBuildApi);
        }
    }

}
