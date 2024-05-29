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

import io.skippy.core.Profiler;
import org.gradle.api.Project;
import org.gradle.api.tasks.testing.Test;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;

import static io.skippy.gradle.SkippyGradleUtils.*;

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
final class SkippyPlugin implements org.gradle.api.Plugin<Project> {

    @Override
    public void apply(Project project) {
        Profiler.clear();
        project.getPlugins().apply(JacocoPlugin.class);
        project.getExtensions().create("skippy", SkippyPluginExtension.class);
        project.getTasks().register("skippyClean", SkippyCleanTask.class, task ->
                task.getSettings().set(CachableProperties.from(project))
        );
        var testFailedListener = new TestFailedListener();
        project.getTasks().register("skippyAnalyze", SkippyAnalyzeTask.class, task -> {
            task.getSettings().set(CachableProperties.from(project));
            task.getTestFailedListener().set(testFailedListener);
        });
        project.getTasks().withType(Test.class, testTask -> {
            testTask.finalizedBy("skippyAnalyze");
            testTask.addTestListener(testFailedListener);
        });
        project.afterEvaluate(action ->
            ifBuildSupportsSkippy(CachableProperties.from(project), skippyBuildApi -> skippyBuildApi.buildStarted())
        );
    }

}