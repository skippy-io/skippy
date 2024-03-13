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

import io.skippy.common.util.Profiler;
import org.gradle.api.Project;
import org.gradle.api.tasks.testing.Test;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;

import static io.skippy.gradle.SkippyGradleUtils.skippyBuildApi;
import static io.skippy.gradle.SkippyGradleUtils.supportsSkippy;

/**
 * The Skippy plugin adds the
 * <ul>
 *     <li>{@link SkippyAnalyzeTask} and </li>
 *     <li>{@link SkippyCleanTask}</li>
 * </ul>
 * tasks to the project.
 *
 * <br /><br />
 *
 * @author Florian McKee
 */
public final class SkippyPlugin implements org.gradle.api.Plugin<Project> {

    @Override
    public void apply(Project project) {
        Profiler.clear();
        project.getPlugins().apply(JacocoPlugin.class);
        var skippyExtension = project.getExtensions().create("skippy", SkippyPluginExtension.class);
        project.getTasks().register("skippyClean", SkippyCleanTask.class);
        project.getTasks().register("skippyAnalyze", SkippyAnalyzeTask.class, skippyExtension);
        project.getTasks().withType(Test.class, testTask -> testTask.finalizedBy("skippyAnalyze"));
        project.afterEvaluate(action -> {
            if (supportsSkippy(action.getProject())) {
                skippyBuildApi(action.getProject()).buildStarted(skippyExtension.toSkippyConfiguration());
            }
        });
    }

}