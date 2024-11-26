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
        project.getTasks().register("skippyClean", SkippyCleanTask.class);
        project.getTasks().register("skippyAnalyze", SkippyAnalyzeTask.class);

        project.afterEvaluate(action -> {

            var projectSettings = ProjectSettings.from(action);

            project.getTasks().withType(SkippyCleanTask.class).forEach( task -> task.getProjectSettings().set(projectSettings));
            project.getTasks().withType(SkippyAnalyzeTask.class).forEach( task -> task.getProjectSettings().set(projectSettings));

            action.getTasks().withType(Test.class, testTask -> {
                testTask.finalizedBy("skippyAnalyze");
                testTask.doFirst(task -> {
                    projectSettings.ifBuildSupportsSkippy(skippyBuildApi -> {
                        var exclusions = skippyBuildApi.getExclusions();
                        testTask.getLogger().lifecycle("Exclusions that will be added: %s".formatted(exclusions.size()));
                        for (var test : exclusions) {
                            var exclusion = "**/%s.*".formatted(test.getClassName().replaceAll("\\.", "/"));
                            testTask.getLogger().lifecycle("Adding exclusion %s".formatted(exclusion));
                            testTask.exclude(exclusion);
                        }
                    });
                });
            });

            projectSettings.ifBuildSupportsSkippy(skippyBuildApi -> skippyBuildApi.buildStarted());
        });
    }

}