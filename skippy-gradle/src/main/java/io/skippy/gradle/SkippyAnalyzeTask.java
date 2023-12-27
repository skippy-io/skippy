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

import io.skippy.build.SkippyBuildApi;
import org.gradle.BuildAdapter;
import org.gradle.BuildResult;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.testing.Test;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension;

import javax.inject.Inject;

import static io.skippy.core.SkippyConstants.SKIPPY_ANALYZE_MARKER;

/**
 *
 * Triggers the execution of all tests by declaring a dependency on the {@code check} lifecycle tasks.
 *
 * <br /><br />
 *
 * It applies the following configuration changes to the project:
 * <ul>
 *     <li>Applies the {@link JacocoPlugin}</li>
 *     <li>Sets the system property {@code skippyEmitCovFiles} to {@code true}</li>
 * </ul>
 *
 * This allows Skippy's JUnit libraries to emit coverages files during the execution of the test suite.
 *
 * <br /><br />
 *
 * The task calls
 * <ul>
 *     <li>{@link SkippyBuildApi#clearSkippyFolder()} upon failure and</l>
 *     <li>{@link SkippyBuildApi#writeClassesMd5FileAndCompactCoverageFiles()} upon success.</l>
 * </ul>
 *
 * <br /><br />
 *
 * Invocation: <code>./gradlew skippyAnalyze</code>.
 *
 * @author Florian McKee
 */
class SkippyAnalyzeTask extends DefaultTask {

    @Inject
    public SkippyAnalyzeTask(SkippyBuildApi skippyBuildApi) {
        setGroup("skippy");

        // set up task dependencies
        for (var sourceSet : getProject().getExtensions().getByType(SourceSetContainer.class)) {
            dependsOn(sourceSet.getClassesTaskName());
        }
        dependsOn("clean", "skippyClean", "check");
        getProject().getTasks().getByName("check").mustRunAfter("clean", "skippyClean");

        doLast((task) -> {
            skippyBuildApi.writeClassesMd5FileAndCompactCoverageFiles();
        });

        if (isSkippyAnalyzeBuild(getProject())) {
            configureSkippyAnalyzeBuild(skippyBuildApi);
        }
    }

    private void configureSkippyAnalyzeBuild(SkippyBuildApi skippyBuildApi) {
        // Skippy's JUnit libraries (e.g., skippy-junit5) rely on the JaCoCo agent to generate coverage data.
        getProject().getPlugins().apply(JacocoPlugin.class);
        getProject().getExtensions().getByType(JacocoPluginExtension.class).setToolVersion(SkippyProperties.getJacocoVersion());

        // This property informs Skippy's JUnit libraries (e.g., skippy-junit5) to emit coverage data for
        // skippified tests.
        getProject().getTasks().withType(Test.class, test -> test.environment(SKIPPY_ANALYZE_MARKER, true));

        clearSkippyFolderUponFailure(skippyBuildApi);
    }

    private void clearSkippyFolderUponFailure(SkippyBuildApi skippyBuildApi) {
        getProject().getGradle().addBuildListener(new BuildAdapter() {
            @Override
            public void buildFinished(BuildResult result) {
                if (result.getFailure() != null) {
                    getLogger().lifecycle("Clearing skippy folder due to build failure");
                    skippyBuildApi.clearSkippyFolder();
                }
            }
        });
    }

    private static boolean isSkippyAnalyzeBuild(Project project) {
        var taskRequests = project.getGradle().getStartParameter().getTaskRequests();
        return taskRequests.stream().anyMatch(request -> request.getArgs().stream().anyMatch(task -> task.endsWith("skippyAnalyze")));
    }

}