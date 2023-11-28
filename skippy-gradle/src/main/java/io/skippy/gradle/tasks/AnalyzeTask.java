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

package io.skippy.gradle.tasks;

import io.skippy.gradle.DecoratedClass;
import io.skippy.gradle.ClassCollector;
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
import java.util.List;

import static io.skippy.gradle.SkippyConstants.SKIPPY_ANALYSIS_FILE;
import static io.skippy.gradle.SkippyConstants.SKIPPY_DIRECTORY;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.Files.writeString;
import static java.util.stream.Collectors.joining;

/**
 * The tasks that is run via <code>./gradlew skippyAnalyze</code>.
 *
 * @author Florian McKee
 */
public class AnalyzeTask extends DefaultTask {

    /**
     * C'tor
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
            createSkippyAnalysisFile(getProject());
        });
    }

    private void createCoverageReportsForSkippifiedTests(Project project) {
        var skippifiedTests = ClassCollector.collect(project).stream().filter(DecoratedClass::usesSkippyExtension).toList();
        GradleConnector connector = GradleConnector.newConnector();
        connector.forProjectDirectory(getProject().getProjectDir());
        try (ProjectConnection connection = connector.connect()) {
            for (var skippifiedTest : skippifiedTests) {
                BuildLauncher build = connection.newBuild();
                build.forTasks("test", "jacocoTestReport");
                build.addArguments("-PskippyCoverageBuild=" + skippifiedTest.getFullyQualifiedClassName());
                if (getLogging().getLevel() != null) {
                    build.addArguments("--" + getLogging().getLevel().name().toLowerCase());
                }
                var errorOutputStream = new ByteArrayOutputStream();
                build.setStandardError(errorOutputStream);

                var standardOutputStream = new ByteArrayOutputStream();
                build.setStandardOutput(standardOutputStream);

                var csvFile = getProject().getProjectDir().toPath().resolve(SKIPPY_DIRECTORY).resolve(skippifiedTest.getFullyQualifiedClassName() + ".csv");
                getLogger().lifecycle("Capturing coverage data for %s in %s".formatted(
                        skippifiedTest.getFullyQualifiedClassName(),
                        getProject().getProjectDir().toPath().relativize(csvFile))
                );
                try {
                    build.run();
                } catch (Exception e) {
                    getLogger().error(e.getMessage(), e);
                    throw e;
                }

                var errors = errorOutputStream.toString();
                if ( ! errors.isEmpty()) {
                    getLogger().error(errorOutputStream.toString());
                }
            }
        }
    }

    private void createSkippyAnalysisFile(Project project) {
        try {
            var skippyAnalysisFile = getProject().getProjectDir().toPath().resolve(SKIPPY_DIRECTORY).resolve(SKIPPY_ANALYSIS_FILE);
            skippyAnalysisFile.toFile().createNewFile();
            var sourceFiles = ClassCollector.collect(project);
            getLogger().lifecycle("Creating the Skippy analysis file %s.".formatted(project.getProjectDir().toPath().relativize(skippyAnalysisFile)));
            logOutputForSkippyFunctionalTest(project, sourceFiles);
            writeString(skippyAnalysisFile, sourceFiles.stream()
                            .map(sourceFile -> "%s:%s".formatted(
                                sourceFile.getClassFileName(getProject().getProjectDir().toPath()),
                                sourceFile.getHash(project.getLogger())
                            ))
                            .collect(joining(lineSeparator())));
            if (getLogger().isInfoEnabled()) {
                getLogger().info("Content of %s: ".formatted(skippyAnalysisFile));
                for (var line: readAllLines(skippyAnalysisFile)) {
                    getLogger().info(line);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Verbose logging with level lifecycle that the functional tests in skippy-gradle rely on.
     */
    private void logOutputForSkippyFunctionalTest(Project project, List<DecoratedClass> sourceFiles) {
        if (sourceFiles.isEmpty() || ! project.hasProperty("skippyFunctionalTest")) {
            return;
        }
        int maxLengthClassName = sourceFiles.stream().mapToInt(it -> it.getFullyQualifiedClassName().length()).max().getAsInt() + 1;
        for (var sourceFile : sourceFiles.subList(0, sourceFiles.size() - 1)) {
            getLogger().lifecycle("+--- "
                    + padRight(sourceFile.getFullyQualifiedClassName(), maxLengthClassName)
                    + sourceFile.getHash(project.getLogger())
            );
        }
        var lastSourceFile = sourceFiles.get(sourceFiles.size() - 1);
        getLogger().lifecycle("\\--- "
                + padRight(lastSourceFile.getFullyQualifiedClassName(), maxLengthClassName)
                + lastSourceFile.getHash(project.getLogger())
        );
    }

    private static String padRight(String s, int count) {
        if (s.length() < count) {
            return s + " ".repeat(count - s.length());
        }
        return s;
    }

}