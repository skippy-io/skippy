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

import io.skippy.core.SkippyBuildApi;
import io.skippy.core.SkippyConfiguration;
import io.skippy.core.SkippyRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Mojo that informs Skippy that a build has started.
 */
@Mojo(name = "buildStarted", defaultPhase = LifecyclePhase.INITIALIZE)
public class SkippyBuildStartedMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(defaultValue = "false", property = "coverageForSkippedTests", required = false)
    private boolean coverageForSkippedTests;

    @Parameter(defaultValue = "false", property = "repository", required = false)
    private String repository;

    @Component
    private MavenSession session;

    @Override
    public void execute() {
        var projectDir = project.getBasedir().toPath();
        var skippyConfiguration = new SkippyConfiguration(coverageForSkippedTests, Optional.ofNullable(repository));
        var skippyApi = new SkippyBuildApi(
                skippyConfiguration,
                new MavenClassFileCollector(project),
                SkippyRepository.getInstance(skippyConfiguration, projectDir, projectDir.resolve(Path.of(project.getBuild().getOutputDirectory()).getParent()))
        );
        skippyApi.buildStarted();
    }

}
