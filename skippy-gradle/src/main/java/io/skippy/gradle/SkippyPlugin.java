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

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.testing.Test;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension;
import org.gradle.testing.jacoco.tasks.JacocoReport;

import java.nio.file.Path;

import static io.skippy.gradle.SkippyConstants.SKIPPY_DIRECTORY;
import static java.util.Arrays.asList;

/**
 * Adds the {@code skippyClean} and {@code skippyAnalyze} tasks to a project.
 *
 * @author Florian McKee
 */
public final class SkippyPlugin implements org.gradle.api.Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);

        var isSkippyCoverageBuild = project.hasProperty("skippyCoverageBuild");
        
        if  (! isSkippyCoverageBuild) {

            // add skippy tasks to the regular build
            project.getTasks().register("skippyClean", CleanTask.class);
            project.getTasks().register("skippyAnalyze", AnalyzeTask.class);
        } else {

            var testClass = new DecoratedClass(project, Path.of(String.valueOf(project.property("skippyClassFile"))));

            // modify test and jacocoTestReport tasks in the skippyCoverage builds
            project.getPlugins().apply(JacocoPlugin.class);
            modifyTestTask(project, testClass);
            modifyJacocoTestReportTask(project, testClass);
        }
    }

    private static void modifyTestTask(Project project, DecoratedClass testClass) {
        project.getTasks().withType(Test.class, test -> {
            test.filter((filter) -> filter.includeTestsMatching(testClass.getFullyQualifiedClassName()));
            test.getExtensions().configure(JacocoTaskExtension.class, jacoco -> {
                jacoco.setDestinationFile(project.file(project.getProjectDir() + "/skippy/" + testClass.getFullyQualifiedClassName() + ".exec"));
            });
        });
    }

    private static void modifyJacocoTestReportTask(Project project, DecoratedClass testClass) {
        project.afterEvaluate(action -> {
            var testTask = testClass.getTestTask().getName();
            project.getTasks().named("jacocoTestReport", JacocoReport.class, jacoco -> {
                jacoco.setDependsOn(asList(testTask));
                jacoco.reports(reports -> {
                    reports.getXml().getRequired().set(Boolean.FALSE);
                    reports.getCsv().getRequired().set(Boolean.TRUE);
                    reports.getHtml().getOutputLocation().set(project.file(project.getBuildDir() + "/jacoco/html/" + testClass.getFullyQualifiedClassName()));
                    var csvFile = project.getProjectDir().toPath().resolve(SKIPPY_DIRECTORY).resolve(testClass.getFullyQualifiedClassName() + ".csv");
                    reports.getCsv().getOutputLocation().set(project.file(csvFile));
                });
                // capture coverage for all source sets
                jacoco.sourceSets(project.getExtensions().getByType(SourceSetContainer.class).toArray(new SourceSet[0]));
            });
        });
    }

}
