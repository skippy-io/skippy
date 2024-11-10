package io.skippy.gradle.android;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.api.BaseVariant;
import io.skippy.core.SkippyBuildApi;
import io.skippy.core.SkippyRepository;
import org.gradle.api.Project;
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;

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
        var buildDir = project.getLayout().getBuildDirectory().getAsFile().get().toPath();
        var skippyExtension = project.getExtensions().getByType(SkippyPluginExtension.class);
        var projectDir = project.getProjectDir().toPath();
        var classesDirs = new ArrayList<File>();

        project.getPlugins().withId("com.android.application", plugin -> {
            var androidExtension = project.getExtensions().getByType(AppExtension.class);
            androidExtension.getApplicationVariants().matching(variant -> variant.getBuildType().getName().equals("debug")).all(variant -> {
                /*
                 * Adds directory with class files built by the Java compiler:
                 *
                 * build/intermediates/javac/debug/classes
                 * build/intermediates/javac/debugUnitTest/classes
                 * ...
                 */
                classesDirs.addAll(getClassesDirForVariant(variant));
                classesDirs.addAll(getClassesDirForVariant(variant.getUnitTestVariant()));
                classesDirs.addAll(getClassesDirForVariant(variant.getTestVariant()));
            });

            /*
             * Adds directory with class files built by the Kotlin compiler:
             *
             * build/tmp/kotlin-classes/debug
             * build/tmp/kotlin-classes/debugUnitTest
             * ...
             */
            for (var kotlinCompile : project.getTasks().withType(KotlinCompile.class).matching(task -> task.getName().contains("Debug"))) {
                classesDirs.addAll(kotlinCompile.getOutputs().getFiles().getFiles());
            }
        });
        return new ProjectSettings(classesDirs, skippyExtension, projectDir, buildDir);
    }

    private static List<File> getClassesDirForVariant(BaseVariant variant) {
        if (variant != null) {
            var classesDirProperty = variant.getJavaCompileProvider().get().getDestinationDirectory();
            return asList(classesDirProperty.getAsFile().get());
        }
        return emptyList();
    }

    void ifBuildSupportsSkippy(Consumer<SkippyBuildApi> action) {
        if (false == classesDirs.isEmpty()) {
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