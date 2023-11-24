package io.skippy.gradle.core;

import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SourceFileCollector {

    public static List<SourceFile> getAllSources(Project project) {
        var result = new ArrayList<SourceFile>();
        var sourceSetContainer = project.getExtensions().getByType(SourceSetContainer.class);
        for (SourceSet sourceSet : sourceSetContainer) {
            result.addAll(getSourceFiles(sourceSet));
        }
        return result;
    }

    private static List<SourceFile> getSourceFiles(SourceSet sourceSet) {
        var javaFiles = sourceSet.getJava().getFiles().stream()
                .map(File::toPath)
                .toList();
        var sourceDirectories = sourceSet.getJava().getSrcDirs().stream()
                .map(File::toPath)
                .toList();

        var result = new ArrayList<SourceFile>();
        for (var javaFile : javaFiles) {
            for (var sourceDirectory : sourceDirectories) {
                if (javaFile.toAbsolutePath().startsWith(sourceDirectory.toAbsolutePath())) {
                    result.add(SourceFile.of(javaFile, sourceDirectory, sourceSet.getJava().getClassesDirectory().get().getAsFile().toPath()));
                    break;
                }
            }
        }
        return result;
    }
}
