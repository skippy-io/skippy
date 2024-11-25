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
import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestListener;
import org.gradle.api.tasks.testing.TestResult;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

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
                        for (var test : skippyBuildApi.getExclusions()) {
                            var exclusion = "**/%s.*".formatted(test.getClassName().replaceAll("\\.", "/"));
                            testTask.getLogger().lifecycle("Adding exclusion %s".formatted(exclusion));
                            testTask.exclude(exclusion);
                        }
                    });
                });
            });


            action.getTasks().withType(Test.class, testTask -> {
                testTask.finalizedBy("skippyAnalyze");
                testTask.addTestListener(new TestListener() {
                    @Override
                    public void beforeSuite(TestDescriptor testDescriptor) {
                    }

                    @Override
                    public void afterSuite(TestDescriptor testDescriptor, TestResult testResult) {
                    }

                    @Override
                    public void beforeTest(TestDescriptor testDescriptor) {
                        var testClass = loadClass(testTask, testDescriptor.getClassName());
                        projectSettings.ifBuildSupportsSkippy(skippyBuildApi -> skippyBuildApi.beforeTest(testClass));
                    }

                    @Override
                    public void afterTest(TestDescriptor testDescriptor, TestResult testResult) {
                        var testClass = loadClass(testTask, testDescriptor.getClassName());
                        projectSettings.ifBuildSupportsSkippy(skippyBuildApi -> skippyBuildApi.afterTest(testClass));
                    }
                });
            });

            projectSettings.ifBuildSupportsSkippy(skippyBuildApi -> skippyBuildApi.buildStarted());
        });
    }

    private Class<?> loadClass(Test testTask, String className) {
        try {
            var urls = testTask.getClasspath().getFiles().stream().map(file -> {
                try {
                    return file.toURI().toURL();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }).toList();
            var classloader = new URLClassLoader(urls.toArray(new URL[]{}));
            return classloader.loadClass(className);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}