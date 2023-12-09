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
import io.skippy.gradle.model.SkippifiedTest;
import org.gradle.api.DefaultTask;
import org.gradle.api.UncheckedIOException;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static io.skippy.gradle.SkippyConstants.SKIPPY_ANALYSIS_FILES_TXT;
import static io.skippy.gradle.SkippyConstants.SKIPPY_DIRECTORY;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.writeString;
import static java.util.stream.Collectors.joining;

/**
 * The tasks that is run via <code>./gradlew skippyAnalyze</code>.
 *
 * @author Florian McKee
 */
class AnalyzeTask extends DefaultTask {

    private final ClassFileCollector classCollector;
    private final SkippifiedTestCollector skippifiedTestCollector;

    /**
     * C'tor.
     *
     * @param classFileCollector
     * @param skippifiedTestCollector
     */
    @Inject
    public AnalyzeTask(ClassFileCollector classFileCollector, SkippifiedTestCollector skippifiedTestCollector) {
        this.classCollector = classFileCollector;
        this.skippifiedTestCollector = skippifiedTestCollector;
        setGroup("skippy");
        dependsOn("skippyClean");
        doLast((task) -> {
            createCoverageReportsForSkippifiedTests();
            createAnalyzedFilesTxt();
        });
    }

    private void createCoverageReportsForSkippifiedTests() {
        GradleConnector connector = GradleConnector.newConnector();
        connector.forProjectDirectory(getProject().getProjectDir());
        try (ProjectConnection connection = connector.connect()) {
            for (var skippifiedTest : skippifiedTestCollector.collect()) {
                runCoverageBuild(connection, skippifiedTest);
            }
        }
    }

    private void runCoverageBuild(ProjectConnection connection, SkippifiedTest skippifiedTest) {
        BuildLauncher buildLauncher = connection.newBuild();
        var errorOutputStream = new ByteArrayOutputStream();
        buildLauncher.setStandardError(errorOutputStream);
        var standardOutputStream = new ByteArrayOutputStream();
        buildLauncher.setStandardOutput(standardOutputStream);
        configureCoverageBuild(buildLauncher, skippifiedTest);
        try {
            long ms = Profiler.stopWatch(() -> buildLauncher.run());
            var errors = errorOutputStream.toString();
            if ( ! errors.isEmpty()) {
                getLogger().error(errors);
            }
            var ouput = standardOutputStream.toString();
            if ( ! ouput.isEmpty()) {
                for (var line : ouput.split(System.lineSeparator())) {
                    getLogger().lifecycle("%s    %s".formatted(skippifiedTest.getFullyQualifiedClassName(), line));
                }
            }
            getLogger().lifecycle("%s".formatted(skippifiedTest.getFullyQualifiedClassName()));
            getLogger().lifecycle("%s Build executed in %sms.".formatted(skippifiedTest.getFullyQualifiedClassName(), ms));
        } catch (Exception e) {
            getLogger().error(e.getMessage(), e);
            throw e;
        }
    }

    private void configureCoverageBuild(BuildLauncher build, SkippifiedTest skippifiedTest) {
        build.forTasks(CoverageBuild.getTasks(skippifiedTest).toArray(new String[0]));
        build.addArguments(CoverageBuild.getArguments(skippifiedTest).toArray(new String[0]));

        var csvFile = getProject().getProjectDir().toPath().resolve(SKIPPY_DIRECTORY).resolve(skippifiedTest.getFullyQualifiedClassName() + ".csv");

        getLogger().lifecycle("\n%s > Capturing coverage data in %s".formatted(
                skippifiedTest.getFullyQualifiedClassName(),
                getProject().getProjectDir().toPath().relativize(csvFile))
        );
        getLogger().lifecycle("%s > ./gradlew %s %s".formatted(
                skippifiedTest.getFullyQualifiedClassName(),
                CoverageBuild.getTasks(skippifiedTest).stream().collect(joining(" ")),
                CoverageBuild.getArguments(skippifiedTest).stream().collect(joining(" "))
        ));
        getLogger().lifecycle("%s".formatted(skippifiedTest.getFullyQualifiedClassName()));
    }

    private void createAnalyzedFilesTxt() {
        try {
            var skippyAnalysisFile = getProject().getProjectDir().toPath().resolve(SKIPPY_DIRECTORY).resolve(SKIPPY_ANALYSIS_FILES_TXT);
            skippyAnalysisFile.toFile().createNewFile();
            getLogger().lifecycle("\nCreating the Skippy analysis file %s.".formatted(getProject().getProjectDir().toPath().relativize(skippyAnalysisFile)));
            var classFiles = classCollector.collect();
            writeString(skippyAnalysisFile, classFiles.stream()
                    .map(classFile -> "%s:%s".formatted(classFile.getRelativePath(), classFile.getHash()))
                    .collect(joining(lineSeparator())));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}