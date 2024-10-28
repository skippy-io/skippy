package io.skippy.gradle;

import io.skippy.core.SkippyBuildApi;
import io.skippy.core.SkippyRepository;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A sub-set of relevant {@link Project} properties that are compatible with Gradle's Configuration Cache.
 */
class ProjectSettings {

    final boolean sourceSetContainerAvailable;
    final List<File> classesDirs;
    final SkippyPluginExtension skippyPluginExtension;
    final Path projectDir;
    final Path buildDir;

    private ProjectSettings(boolean sourceSetContainerAvailable, List<File> classesDirs, SkippyPluginExtension skippyExtension, Path projectDir, Path buildDir) {
        this.sourceSetContainerAvailable = sourceSetContainerAvailable;
        this.classesDirs = classesDirs;
        this.skippyPluginExtension = skippyExtension;
        this.projectDir = projectDir;
        this.buildDir = buildDir;
    }

    static ProjectSettings from(Project project) {
        var sourceSetContainer = project.getExtensions().findByType(SourceSetContainer.class);
        if (sourceSetContainer != null) {
            // new ArrayList<>() is a workaround for https://github.com/gradle/gradle/issues/26942
            var classesDirs = new ArrayList<>(sourceSetContainer.stream().flatMap(sourceSet -> sourceSet.getOutput().getClassesDirs().getFiles().stream()).toList());
            var skippyExtension = project.getExtensions().getByType(SkippyPluginExtension.class);
            var projectDir = project.getProjectDir().toPath();
            var buildDir = project.getLayout().getBuildDirectory().getAsFile().get().toPath();
            return new ProjectSettings(sourceSetContainer != null, classesDirs, skippyExtension, projectDir, buildDir);
        } else {
            return new ProjectSettings(false, null, null, null, null);
        }
    }

    void ifBuildSupportsSkippy(Consumer<SkippyBuildApi> action) {
        if (sourceSetContainerAvailable) {
            var skippyConfiguration = skippyPluginExtension.toSkippyConfiguration();
            var skippyBuildApi = new SkippyBuildApi(
                    skippyConfiguration,
                    new GradleClassFileCollector(projectDir, classesDirs),
                    SkippyRepository.getInstance(
                            skippyConfiguration,
                            projectDir,
                            buildDir
                    )
            );
            action.accept(skippyBuildApi);
        }
    }

}