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

package io.skippy.maven;

import io.skippy.build.SkippyBuildApi;
import io.skippy.common.model.SkippyConfiguration;
import io.skippy.common.repository.SkippyRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Mojo that informs Skippy that the parts of the build that are relevant for Skippy (e.g., compilation and test
 * execution) have finished.
 */
@Mojo(name = "buildFinished", defaultPhase = LifecyclePhase.TEST)
public class SkippyBuildFinishedMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Component
    private MavenSession session;

    @Override
    public void execute() {
        var projectDir = project.getBasedir().toPath();
        var skippyBuildApi = new SkippyBuildApi(
            projectDir,
            new MavenClassFileCollector(project),
            SkippyRepository.getInstance(projectDir)
        );
        skippyBuildApi.buildFinished(new SkippyConfiguration(false));
    }

}
