package io.skippy.gradle.core;

import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.util.Comparator.comparing;

/**
 * Analyzes all sources in a project.
 */
public class Analyzer {

    /**
     * Analyzes all sources in a project.
     *
     * @param project a {@link Project}
     * @return a list of {@link AnalyzedFile}s
     */
    public static List<AnalyzedFile> analyzeProject(Project project) {
        var result = new ArrayList<AnalyzedFile>();
        var sourceSetContainer = project.getExtensions().getByType(SourceSetContainer.class);
        for (SourceSet sourceSet : sourceSetContainer) {
            result.addAll(analyzeSourceSet(sourceSet));
        }
        return result.stream().sorted(comparing(AnalyzedFile::getFullyQualifiedClassName)).toList();
    }

    private static List<AnalyzedFile> analyzeSourceSet(SourceSet sourceSet) {
        var javaFiles = sourceSet.getJava().getFiles().stream()
                .map(File::toPath)
                .toList();
        var sourceDirectories = sourceSet.getJava().getSrcDirs().stream()
                .map(File::toPath)
                .toList();

        var result = new ArrayList<AnalyzedFile>();
        for (var javaFile : javaFiles) {
            for (var sourceDirectory : sourceDirectories) {
                if (javaFile.toAbsolutePath().startsWith(sourceDirectory.toAbsolutePath())) {
                    result.add(AnalyzedFile.of(javaFile, sourceDirectory, sourceSet.getJava().getClassesDirectory().get().getAsFile().toPath()));
                    break;
                }
            }
        }
        return result;
    }
}
