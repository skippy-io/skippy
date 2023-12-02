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

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.UncheckedIOException;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static io.skippy.gradle.Profiler.profile;
import static io.skippy.gradle.SkippyConstants.SKIPPY_ANALYSIS_FILES_TXT;
import static io.skippy.gradle.SkippyConstants.SKIPPY_DIRECTORY;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.writeString;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

/**
 * The tasks that is run via <code>./gradlew skippyAnalyze</code>.
 *
 * @author Florian McKee
 */
class AnalyzeTask extends DefaultTask {

    /**
     * Comment to make the JavaDoc task happy.
     */
    @Inject
    public AnalyzeTask() {
        setGroup("skippy");
        var dependencies = new ArrayList<String>();
        dependencies.add("classes");
        dependencies.add("testClasses");
        dependencies.add("skippyClean");
        setDependsOn(dependencies);
        doLast((task) -> {
            createCoverageReportsForSkippifiedTests(getProject());
            createAnalyzedFilesTxt(getProject());
            Profiler.printResults(getLogger());
        });
    }

    private void createCoverageReportsForSkippifiedTests(Project project) {
        GradleConnector connector = GradleConnector.newConnector();
        connector.forProjectDirectory(getProject().getProjectDir());
        try (ProjectConnection connection = connector.connect()) {
            for (var skippifiedTest : DecoratedClass.fromAllSkippifiedTestsIn(project)) {
                runCoverageBuild(connection, skippifiedTest);
            }
        }
    }

    private void runCoverageBuild(ProjectConnection connection, DecoratedClass skippifiedTest) {
        profile(AnalyzeTask.class, "runCoverageBuild", () -> {
            BuildLauncher buildLauncher = connection.newBuild();
            var errorOutputStream = new ByteArrayOutputStream();
            buildLauncher.setStandardError(errorOutputStream);
            var standardOutputStream = new ByteArrayOutputStream();
            buildLauncher.setStandardOutput(standardOutputStream);
            configureCoverageBuild(buildLauncher, skippifiedTest);
            try {
                buildLauncher.run();
            } catch (Exception e) {
                getLogger().error(e.getMessage(), e);
                throw e;
            }
            var errors = errorOutputStream.toString();
            if ( ! errors.isEmpty()) {
                getLogger().error(errorOutputStream.toString());
            }
        });
    }

    private void configureCoverageBuild(BuildLauncher build, DecoratedClass skippifiedTest) {
        profile(AnalyzeTask.class, "configureCoverageBuild", () -> {
            var tasks = asList(skippifiedTest.getTestTask().getName(), "jacocoTestReport");
            var arguments = asList("-PskippyCoverageBuild=true", "-PskippyClassFile=" + skippifiedTest.getAbsolutePath());
            build.forTasks(tasks.toArray(new String[0]));
            build.addArguments(arguments.toArray(new String[0]));
            if (getLogging().getLevel() != null) {
                build.addArguments("--" + getLogging().getLevel().name().toLowerCase());
            }
            var csvFile = getProject().getProjectDir().toPath().resolve(SKIPPY_DIRECTORY).resolve(skippifiedTest.getFullyQualifiedClassName() + ".csv");
            getLogger().lifecycle("Capturing coverage data for %s in %s".formatted(
                    skippifiedTest.getFullyQualifiedClassName(),
                    getProject().getProjectDir().toPath().relativize(csvFile))
            );
            getLogger().info("./gradlew %s %s".formatted(
                    tasks.stream().collect(joining(" ")),
                    arguments.stream().collect(joining(" "))
            ));
        });
    }

    private void createAnalyzedFilesTxt(Project project) {
        profile(AnalyzeTask.class, "createAnalyzedFilesTxt", () -> {
            try {
                var skippyAnalysisFile = getProject().getProjectDir().toPath().resolve(SKIPPY_DIRECTORY).resolve(SKIPPY_ANALYSIS_FILES_TXT);
                skippyAnalysisFile.toFile().createNewFile();
                var classFiles = DecoratedClass.fromAllClassesIn(project);
                getLogger().lifecycle("Creating the Skippy analysis file %s.".formatted(project.getProjectDir().toPath().relativize(skippyAnalysisFile)));
                writeString(skippyAnalysisFile, classFiles.stream()
                        .map(classFile -> "%s:%s".formatted(classFile.getRelativePath(), classFile.getHash()))
                        .collect(joining(lineSeparator())));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

}