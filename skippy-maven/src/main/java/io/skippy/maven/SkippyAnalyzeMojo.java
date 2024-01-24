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
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import static io.skippy.common.SkippyConstants.TEST_IMPACT_ANALYSIS_RUNNING;

/**
 * Compacts the {@code .cov} file in the skippy folder and writes the {@code classes.md5} file.
 * <br /><br />
 * Invocation: {@code ./gradlew skippyAnalyze}.
 *
 * @author Florian McKee
 */
@Mojo(name = "analyze", defaultPhase = LifecyclePhase.TEST)
public class SkippyAnalyzeMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Component
    private MavenSession session;

    @Override
    public void execute() {
        if (executeGoal()) {
            var skippyBuildApi = new SkippyBuildApi(project.getBasedir().toPath(), new MavenClassFileCollector(project));
            project.getProperties().setProperty(TEST_IMPACT_ANALYSIS_RUNNING, "true");
            skippyBuildApi.upsertTestImpactAnalysisJson();
            getLog().info("skippy:analyze executed");
        } else {
            getLog().info("skippy:analyze skipped");
        }
    }

    private boolean executeGoal() {
        var isSkippyAnalyzeBuild = Boolean.valueOf(System.getProperty(TEST_IMPACT_ANALYSIS_RUNNING));
        var goalExecutedDirectly = session.getRequest().getGoals().contains("skippy:analyze");
        return isSkippyAnalyzeBuild || goalExecutedDirectly;
    }

}
