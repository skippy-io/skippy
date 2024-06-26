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

import java.util.function.Consumer;

final class SkippyGradleUtils {

    static void ifBuildSupportsSkippy(CachableProperties settings, Consumer<SkippyBuildApi> action) {
        if (settings.sourceSetContainerAvailable) {
            var skippyConfiguration = settings.skippyPluginExtension.toSkippyConfiguration();
            var projectDir = settings.projectDir;
            var skippyBuildApi = new SkippyBuildApi(
                    skippyConfiguration,
                    new GradleClassFileCollector(projectDir, settings.classesDirs),
                    SkippyRepository.getInstance(
                            skippyConfiguration,
                            projectDir,
                            settings.buildDir
                    )
            );
            action.accept(skippyBuildApi);
        }
    }

}
