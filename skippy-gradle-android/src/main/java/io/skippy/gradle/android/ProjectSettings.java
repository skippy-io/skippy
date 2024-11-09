package io.skippy.gradle.android;

import io.skippy.core.SkippyBuildApi;
import org.gradle.api.Project;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * A sub-set of relevant {@link Project} properties that are compatible with Gradle's Configuration Cache.
 */
class ProjectSettings implements Serializable {

    final List<File> classesDirs;
    final SkippyPluginExtension skippyPluginExtension;
    final Path projectDir;
    final Path buildDir;

    private ProjectSettings(List<File> classesDirs, SkippyPluginExtension skippyExtension, Path projectDir, Path buildDir) {
        this.classesDirs = classesDirs;
        this.skippyPluginExtension = skippyExtension;
        this.projectDir = projectDir;
        this.buildDir = buildDir;
    }

    static ProjectSettings from(Project project) {
        return new ProjectSettings(null, null, null, null);
    }

    void ifBuildSupportsSkippy(Consumer<SkippyBuildApi> action) {
    }

}