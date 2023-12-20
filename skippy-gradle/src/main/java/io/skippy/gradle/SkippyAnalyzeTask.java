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

import io.skippy.gradle.io.ClassesMd5Writer;
import io.skippy.gradle.io.CoverageFileCompactor;
import io.skippy.gradle.model.SkippyProperties;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.testing.Test;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension;

import javax.inject.Inject;

/**
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
 * After the execution of the tests, the plugin will
 * <ul>
 *     <li>compact the coverage files (see {@link CoverageFileCompactor}) and</li>
 *     <li>write the {@code classes.md5} file containing hashes for all class file (see {@link ClassesMd5Writer}).</li>
 * </ul>

 * <br /><br />
 *
 * Invocation: <code>./gradlew skippyAnalyze</code>.
 *
 * @author Florian McKee
 */
class SkippyAnalyzeTask extends DefaultTask {

    /**
     * C'tor.
     *
     * @param classesMd5Writer
     * @param coverageFileCompactor
     */
    @Inject
    public SkippyAnalyzeTask(ClassesMd5Writer classesMd5Writer, CoverageFileCompactor coverageFileCompactor) {
        setGroup("skippy");

        // set up task dependencies
        for (var sourceSet : getProject().getExtensions().getByType(SourceSetContainer.class)) {
            dependsOn(sourceSet.getClassesTaskName());
        }
        dependsOn("clean", "skippyClean", "check");
        getProject().getTasks().getByName("check").mustRunAfter("clean", "skippyClean");

        doLast((task) -> {
            classesMd5Writer.write(getLogger(), getProject().getProjectDir().toPath());
            coverageFileCompactor.compact(getLogger(), getProject().getProjectDir().toPath());
        });

        // Skippy's JUnit libraries (e.g., skippy-junit5) rely on the JaCoCo agent to generate coverage data.
        getProject().getPlugins().apply(JacocoPlugin.class);
        getProject().getExtensions().getByType(JacocoPluginExtension.class).setToolVersion(SkippyProperties.getJacocoVersion());

        // This property informs Skippy's JUnit libraries (e.g., skippy-junit5) to emit coverage data for
        // skippified tests.
        getProject().getTasks().withType(Test.class, test -> test.environment("skippyEmitCovFiles", true));
    }

}