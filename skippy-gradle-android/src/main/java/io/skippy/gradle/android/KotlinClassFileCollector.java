package io.skippy.gradle.android;

import org.gradle.api.Project;
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileTool;

import java.io.File;
import java.util.stream.Stream;

public class KotlinClassFileCollector {
    private KotlinClassFileCollector() {}

    static Stream<File> collect(Project project) {
        return project.getTasks().stream()
                .filter(task -> task.getName().startsWith("compile") && task.getName().endsWith("Kotlin"))
                .filter(task -> task instanceof KotlinCompileTool)
                .map(task -> (KotlinCompileTool) task)
                .map(kotlinCompileTool -> kotlinCompileTool.getDestinationDirectory().get().getAsFile());
    }

    static Stream<File> collectIfExists(Project project) {
        return collect(project)
                .filter(File::exists);
    }
}
