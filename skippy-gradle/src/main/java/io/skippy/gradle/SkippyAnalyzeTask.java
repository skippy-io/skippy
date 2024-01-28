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
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.testing.Test;

import javax.inject.Inject;

import io.skippy.common.SkippyConstants;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;

/**
 * Triggers a Skippy analysis that will populate the skippy folder with
 * <ul>
 *     <li>a {@code .cov} file containing coverage data for each skippified test and </l>
 *     <li>a {@code classes.md5} file containing hashes for all class files in the project's output folders.</l>
 * </ul>
 *
 * Invocation: {@code ./gradlew skippyAnalyze}
 *
 * @author Florian McKee
 */
class SkippyAnalyzeTask extends DefaultTask {

    @Inject
    public SkippyAnalyzeTask() {
        setGroup("skippy");
        getProject().getPlugins().apply(JacocoPlugin.class);
        SkippyBuildApiFactory.getInstanceFor(this).ifPresent(skippyBuildApi -> {
            // set up task dependencies
            for (var sourceSet : getProject().getExtensions().getByType(SourceSetContainer.class)) {
                dependsOn(sourceSet.getClassesTaskName());
            }
            dependsOn("clean", "skippyClean", "check");
            getProject().getTasks().getByName("check").mustRunAfter("clean", "skippyClean");

            doLast(task -> skippyBuildApi.upsertTestImpactAnalysisJson());

            getProject().getTasks().withType(Test.class, test ->
                    test.environment(SkippyConstants.TEST_IMPACT_ANALYSIS_RUNNING, true)
            );
        });
    }

}