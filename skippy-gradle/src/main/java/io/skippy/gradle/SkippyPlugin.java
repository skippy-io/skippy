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
import io.skippy.gradle.model.SkippifiedTest;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSetContainer;

/**
 * Adds Skippy support for Gradle.
 * <br />
 * <br />
 * In order to understand how the plugin works, it's important to distinguish between two types of builds:
 * <ul>
 *      <li>Regular builds </li>
 *      <li>Coverage builds for skippified tests</li>
 * </ul>
 *
 * Regular build are started by the user directly (e.g., by invoking {@code ./gradlew skippyAnalyze} on the command line).
 * Coverage builds for skippified tests are started by the {@link AnalyzeTask} for each {@link SkippifiedTest} in the
 * project. Coverage builds are started automatically.
 *
 * <br />
 * <br />
 * Consider the following example:
 *
 * <pre>
 * // regular build
 * ./gradlew skippyAnalyze
 *   │
 *   │  // coverage build
 *   ├─ ./gradlew test jacocoTestReport -PskippyCoverageBuild=true -PskippyClassFile=[..]/FooTest.class ...
 *   │
 *   │  // coverage build
 *   └─ ./gradlew test jacocoTestReport -PskippyCoverageBuild=true -PskippyClassFile=[..]/BarTest.class ...
 * </pre>
 *
 * {@code ./gradlew skippyAnalyze} starts a regular build. Under the hood, {@code skippyAnalyze}
 * starts coverage builds to capture coverage information for tests {@code FooTest} and {@code BarTest}.
 *
 * <br />
 * <br />
 *
 * The Skippy Plugin distinguishes between two cases:
 * <ol>
 *     <li>Regular build: Add the {@code skippyClean} and {@code skippyAnalyze} tasks</li>
 *     <li>Coverage build: Apply the necessary configuration changes for the coverage build (e.g., update the test task
 *     to only run a single test)</li>
 * </ol>
 *
 * @author Florian McKee
 */
public final class SkippyPlugin implements org.gradle.api.Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);

        var skippyExtension = project.getExtensions().create("skippy", SkippyPluginExtension.class);

        if  (CoverageBuild.isCoverageBuild(project)) {

            // this is a coverage build: configure the project accordingly
            CoverageBuild.configure(project);

        } else {

            // this is a regular build: add skippyClean + skippyAnalyze tasks
            project.getTasks().register("skippyClean", CleanTask.class);

            var sourceSetContainer = project.getExtensions().getByType(SourceSetContainer.class);
            var classFileCollector = new ClassFileCollector(project, sourceSetContainer);
            var skippifiedTestCollector = new SkippifiedTestCollector(classFileCollector, sourceSetContainer, skippyExtension);

            project.getTasks().register("skippyAnalyze", AnalyzeTask.class, classFileCollector, skippifiedTestCollector);
        }
    }

}