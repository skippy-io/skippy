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
import io.skippy.gradle.collector.SkippifiedTestCollector;
import io.skippy.gradle.coveragebuild.CoverageBuild;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSetContainer;

/**
 * Adds the {@code skippyClean} and {@code skippyAnalyze} tasks to a project.
 *
 * @author Florian McKee
 */
public final class SkippyPlugin implements org.gradle.api.Plugin<Project> {


        @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);

        var skippyExtension = project.getExtensions().create("skippy", SkippyPluginExtension.class);

        if  (CoverageBuild.isCoverageBuild(project)) {

            // this is a nested coverage build: configure the project
            CoverageBuild.configure(project);

        } else {

            // this is a regular build: add skippyClean + skippyAnalyze tasks
            project.getTasks().register("skippyClean", CleanTask.class);

            var sourceSetContainer = project.getExtensions().getByType(SourceSetContainer.class);
            var classFileCollector = new ClassFileCollector(project, sourceSetContainer);
            var skippifiedTestCollector = new SkippifiedTestCollector(project, classFileCollector, sourceSetContainer, skippyExtension);

            project.getTasks().register("skippyAnalyze", AnalyzeTask.class, classFileCollector, skippifiedTestCollector);
        }
    }

}