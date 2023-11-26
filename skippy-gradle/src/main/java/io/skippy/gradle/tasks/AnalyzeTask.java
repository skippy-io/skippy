package io.skippy.gradle.tasks;

import io.skippy.gradle.core.AnalyzedFile;
import io.skippy.gradle.core.Analyzer;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.UncheckedIOException;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.skippy.gradle.core.SkippyConstants.SKIPPY_ANALYSIS_FILE;
import static io.skippy.gradle.core.SkippyConstants.SKIPPY_DIRECTORY;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.Files.writeString;
import static java.util.stream.Collectors.joining;

/**
 * The tasks that is run via <code>./gradlew skippyAnalyze</code>.
 */
public class AnalyzeTask extends DefaultTask {

    /**
     * C'tor
     *
     * @param coverageTasks the names of {@link CoverageTask}s that this task depends on.
     */
    @Inject
    public AnalyzeTask(List<String> coverageTasks) {
        setGroup("skippy");
        var dependencies = new ArrayList<String>();
        dependencies.add("skippyClean");
        dependencies.addAll(coverageTasks);
        setDependsOn(dependencies);
        onlyIf((task) -> coverageTasks.size() > 0);
        if (coverageTasks.isEmpty()) {
            getLogger().warn("No skippified tests found.");
        }
        doLast((task) -> createSkippyAnalysisFile(getProject()));
    }

    private void createSkippyAnalysisFile(Project project) {
        try {
            var skippyAnalysisFile = getProject().getProjectDir().toPath().resolve(SKIPPY_DIRECTORY).resolve(SKIPPY_ANALYSIS_FILE);
            skippyAnalysisFile.toFile().createNewFile();
            var sourceFiles = Analyzer.analyzeProject(project);
            getLogger().lifecycle("Creating the Skippy analysis file %s.".formatted(project.getProjectDir().toPath().relativize(skippyAnalysisFile)));
            logOutputForSkippyFunctionalTest(project, sourceFiles);
            writeString(skippyAnalysisFile, sourceFiles.stream()
                            .map(sourceFile -> "%s:%s:%s:%s:%s".formatted(
                                sourceFile.getFullyQualifiedClassName(),
                                    sourceFile.getSourceFileName(),
                                    sourceFile.getClassFileName(),
                                    sourceFile.getSourceFileHash(project.getLogger()),
                                    sourceFile.getClassFileHash(project.getLogger())
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
    private void logOutputForSkippyFunctionalTest(Project project, List<AnalyzedFile> sourceFiles) {
        if (sourceFiles.isEmpty() || ! project.hasProperty("skippyFunctionalTest")) {
            return;
        }
        int maxLengthClassName = sourceFiles.stream().mapToInt(it -> it.getFullyQualifiedClassName().length()).max().getAsInt() + 1;
        for (var sourceFile : sourceFiles.subList(0, sourceFiles.size() - 1)) {
            getLogger().lifecycle("+--- "
                    + padRight(sourceFile.getFullyQualifiedClassName(), maxLengthClassName)
                    + sourceFile.getSourceFileHash(project.getLogger())
                    + " "
                    + sourceFile.getClassFileHash(project.getLogger())
            );
        }
        var lastSourceFile = sourceFiles.get(sourceFiles.size() - 1);
        getLogger().lifecycle("\\--- "
                + padRight(lastSourceFile.getFullyQualifiedClassName(), maxLengthClassName)
                + lastSourceFile.getSourceFileHash(project.getLogger())
                + " "
                + lastSourceFile.getClassFileHash(project.getLogger())
        );
    }

    private static String padRight(String s, int count) {
        if (s.length() < count) {
            return s + " ".repeat(count - s.length());
        }
        return s;
    }

}