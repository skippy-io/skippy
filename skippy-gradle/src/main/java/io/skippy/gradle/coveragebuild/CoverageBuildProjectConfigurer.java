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

package io.skippy.gradle.coveragebuild;

import io.skippy.gradle.model.ClassFile;
import io.skippy.gradle.model.SkippifiedTest;
import org.gradle.api.Project;
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
 * Configures a {@code project} for the execution of a coverage build for a skippified tests.
 *
 * @author Florian McKee
 */
final class CoverageBuildProjectConfigurer {

    /**
     * Configures the {@code project} for the execution of a coverage build for a skippified test.
     * <br /><br />
     * Note: The skippified test is inferred from the build arguments.
     *
     * @param project
     */
    static void configure(Project project) {
        var skippifiedTest = getSkippifiedTest(project);
        project.getPlugins().apply(JacocoPlugin.class);
        configureTestTask(project, skippifiedTest);
        configureJacocoTestReportTask(project, skippifiedTest);
    }

    private static SkippifiedTest getSkippifiedTest(Project project) {
        var classFile = Path.of(String.valueOf(project.property("skippyClassFile")));
        var testTaskName = String.valueOf(project.property("skippyTestTask"));
        return new SkippifiedTest(new ClassFile(project, classFile), testTaskName);
    }

    private static void configureTestTask(Project project, SkippifiedTest skippifiedTest) {
        project.getTasks().withType(Test.class, test -> {
            test.filter((filter) -> filter.includeTestsMatching(skippifiedTest.classFile().getFullyQualifiedClassName()));
            test.getExtensions().configure(JacocoTaskExtension.class, jacoco -> {
                jacoco.setDestinationFile(project.file(project.getProjectDir() + "/skippy/" + skippifiedTest.classFile().getFullyQualifiedClassName() + ".exec"));
            });
        });
    }

    private static void configureJacocoTestReportTask(Project project, SkippifiedTest skippifiedTest) {
        project.afterEvaluate(action -> {
            var testTask = skippifiedTest.testTask();
            project.getTasks().named("jacocoTestReport", JacocoReport.class, jacoco -> {
                jacoco.setDependsOn(asList(testTask));
                jacoco.reports(reports -> {
                    reports.getXml().getRequired().set(Boolean.FALSE);
                    reports.getCsv().getRequired().set(Boolean.TRUE);
                    reports.getHtml().getOutputLocation().set(project.file(project.getBuildDir() + "/jacoco/html/" + skippifiedTest.classFile().getFullyQualifiedClassName()));
                    var csvFile = project.getProjectDir().toPath().resolve(SKIPPY_DIRECTORY).resolve(skippifiedTest.classFile().getFullyQualifiedClassName() + ".csv");
                    reports.getCsv().getOutputLocation().set(project.file(csvFile));
                });
                // capture coverage for all source sets
                jacoco.sourceSets(project.getExtensions().getByType(SourceSetContainer.class).toArray(new SourceSet[0]));
            });
        });
    }

}