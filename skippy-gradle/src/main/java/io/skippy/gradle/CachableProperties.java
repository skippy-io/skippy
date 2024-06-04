package io.skippy.gradle;

import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A sub-set of relevant {@link Project} properties that are compatible with Gradle's Configuration Cache.
 */
class CachableProperties {

    final boolean sourceSetContainerAvailable;
    final List<File> classesDirs;
    final SkippyPluginExtension skippyPluginExtension;
    final Path projectDir;
    final Path buildDir;

    private CachableProperties(boolean sourceSetContainerAvailable, List<File> classesDirs, SkippyPluginExtension skippyExtension, Path projectDir, Path buildDir) {
        this.sourceSetContainerAvailable = sourceSetContainerAvailable;
        this.classesDirs = classesDirs;
        this.skippyPluginExtension = skippyExtension;
        this.projectDir = projectDir;
        this.buildDir = buildDir;
    }

    static CachableProperties from(Project project) {
        var sourceSetContainer = project.getExtensions().findByType(SourceSetContainer.class);
        // new ArrayList<>() is a workaround for https://github.com/gradle/gradle/issues/26942
        var classesDirs = new ArrayList<>(sourceSetContainer.stream().flatMap(sourceSet -> sourceSet.getOutput().getClassesDirs().getFiles().stream()).toList());
        var skippyExtension = project.getExtensions().getByType(SkippyPluginExtension.class);
        var projectDir = project.getProjectDir().toPath();
        var buildDir = project.getLayout().getBuildDirectory().getAsFile().get().toPath();
        return new CachableProperties(sourceSetContainer != null, classesDirs, skippyExtension, projectDir, buildDir);
    }

}