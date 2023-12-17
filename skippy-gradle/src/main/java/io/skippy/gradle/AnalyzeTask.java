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
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.SourceSetContainer;

import javax.inject.Inject;

/**
 * The tasks that is run via <code>./gradlew skippyAnalyze</code>.
 *
 * @author Florian McKee
 */
class AnalyzeTask extends DefaultTask {

    /**
     * C'tor.
     *
     * @param classesMd5Writer
     * @param coverageFileCompactor
     */
    @Inject
    public AnalyzeTask(ClassesMd5Writer classesMd5Writer, CoverageFileCompactor coverageFileCompactor) {
        setGroup("skippy");
        for (var sourceSet : getProject().getExtensions().getByType(SourceSetContainer.class)) {
            dependsOn(sourceSet.getClassesTaskName());
        }
        dependsOn("clean", "skippyClean", "check");
        getProject().getTasks().getByName("check").mustRunAfter("clean", "skippyClean");

        doLast((task) -> {
            classesMd5Writer.write(getLogger(), getProject().getProjectDir().toPath());
            coverageFileCompactor.compact(getLogger(), getProject().getProjectDir().toPath());
        });
    }

}