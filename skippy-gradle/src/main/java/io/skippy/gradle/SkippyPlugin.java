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

import org.gradle.StartParameter;
import org.gradle.TaskExecutionRequest;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.testing.Test;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;

import java.util.List;

/**
 * Adds Skippy support for Gradle.
 *
 * @author Florian McKee
 */
public final class SkippyPlugin implements org.gradle.api.Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);

        project.getTasks().register("skippyClean", CleanTask.class);
        project.getTasks().register("skippyAnalyze", AnalyzeTask.class);

        project.getTasks().getByName("check").mustRunAfter("skippyClean");

        if (isSkippyAnalyzeBuild(project)) {
            project.getPlugins().apply(JacocoPlugin.class);
            project.getTasks().withType(Test.class, test -> test.environment("skippyAnalyzeBuild", true));
        }
    }

    private static boolean isSkippyAnalyzeBuild(Project project) {
        var gradle = project.getGradle();
        StartParameter startParameter = gradle.getStartParameter();
        List<TaskExecutionRequest> taskRequests = startParameter.getTaskRequests();
        boolean skippyAnalyze = taskRequests.stream().anyMatch(request -> request.getArgs().stream().anyMatch(task -> task.endsWith("skippyAnalyze")));
        return skippyAnalyze;
    }

}