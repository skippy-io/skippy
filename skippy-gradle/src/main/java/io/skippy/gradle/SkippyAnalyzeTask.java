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

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.services.ServiceReference;
import org.gradle.api.tasks.Internal;

import javax.inject.Inject;

import static io.skippy.gradle.SkippyGradleUtils.*;

/**
 * Informs Skippy that the relevant parts of the build (e.g., compilation and testing) have finished.
 */
abstract class SkippyAnalyzeTask extends DefaultTask {

    @Internal
    abstract Property<CachableProperties> getSettings();

    @ServiceReference
    abstract Property<TestResultService> getTestResultService();

    @Inject
    public SkippyAnalyzeTask() {
        setGroup("skippy");
        doLast(task -> {
            ifBuildSupportsSkippy(getSettings().get(), skippyBuildApi -> {
                for (var failedTest : getTestResultService().get().failedTests) {
                    skippyBuildApi.testFailed(failedTest.getClassName());
                }
                skippyBuildApi.buildFinished();
            });
        });
    }

}