package io.skippy.gradle.android;

import org.gradle.api.Project;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.util.stream.Stream;

public class AndroidClassFileCollector {
    private AndroidClassFileCollector() {}

    static Stream<File> collect(Project project) {
        return project.getTasks()
                .withType(JavaCompile.class)
                .stream()
                .map(task -> task.getDestinationDirectory().getAsFile().get());
    }

    static Stream<File> collectIfExists(Project project) {
        return collect(project)
                .filter(File::exists);
    }
}
