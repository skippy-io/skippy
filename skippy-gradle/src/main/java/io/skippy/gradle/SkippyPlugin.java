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

import io.skippy.gradle.collector.ClassFileCollector;
import io.skippy.gradle.io.ClassesMd5Writer;
import io.skippy.gradle.io.CoverageFileCompactor;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSetContainer;
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
 * @author Florian McKee
 */
public final class SkippyPlugin implements org.gradle.api.Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);
        project.getPlugins().apply(JacocoPlugin.class);

        // some DIY dependency injection
        var classFileCollector = new ClassFileCollector(project, project.getExtensions().getByType(SourceSetContainer.class));
        var classesMd5Writer = new ClassesMd5Writer(classFileCollector);
        var coverageFileCompactor = new CoverageFileCompactor(classFileCollector);

        project.getTasks().register("skippyClean", SkippyCleanTask.class);
        project.getTasks().register("skippyAnalyze", SkippyAnalyzeTask.class, classesMd5Writer, coverageFileCompactor);

        if (skippyAnalyzeTaskRequested(project)) {

            // Skippy's JUnit libraries (e.g., skippy-junit5) use the JaCoCo agent to generate coverage data.
            project.getPlugins().apply(JacocoPlugin.class);

            // This property informs Skippy's JUnit libraries (e.g., skippy-junit5) to emit coverage data for
            // skippified tests.
            project.getTasks().withType(Test.class, test -> test.environment("skippyEmitCovFiles", true));
        }
    }

    private static boolean skippyAnalyzeTaskRequested(Project project) {
        var taskRequests = project.getGradle().getStartParameter().getTaskRequests();
        return taskRequests.stream().anyMatch(request -> request.getArgs().stream().anyMatch(task -> task.endsWith("skippyAnalyze")));
    }

}