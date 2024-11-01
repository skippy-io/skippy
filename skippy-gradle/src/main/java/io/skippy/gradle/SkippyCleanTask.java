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
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

import javax.inject.Inject;

/**
 * Clears the skippy directory.
 * <br /><br />
 * Invocation: {@code ./gradlew skippyClean}
 *
 * @author Florian McKee
 */
abstract class SkippyCleanTask extends DefaultTask {

    @Input
    abstract Property<ProjectSettings> getProjectSettings();

    @Inject
    public SkippyCleanTask() {
        setGroup("skippy");
        doLast(task -> getProjectSettings().get().ifBuildSupportsSkippy(SkippyBuildApi::deleteSkippyFolder));
    }

}
