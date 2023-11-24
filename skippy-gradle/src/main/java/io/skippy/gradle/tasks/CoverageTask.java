package io.skippy.gradle.tasks;

import io.skippy.gradle.core.SourceFile;
import org.gradle.api.DefaultTask;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;

import static io.skippy.gradle.core.SkippyConstants.SKIPPY_DIRECTORY;
import static java.util.Arrays.asList;

public class CoverageTask extends DefaultTask {

    @Inject
    public CoverageTask(SourceFile test) {
        setGroup("skippy (internal)");
        setMustRunAfter(asList("skippyClean"));

        doLast(task -> {
            GradleConnector connector = GradleConnector.newConnector();
            connector.forProjectDirectory(getProject().getProjectDir());
            try (ProjectConnection connection = connector.connect()) {
                BuildLauncher build = connection.newBuild();
                build.forTasks("test", "jacocoTestReport");
                build.addArguments("-PskippyCoverageBuild=" + test.getFullyQualifiedClassName());
                if (getLogging().getLevel() != null) {
                    build.addArguments("--" + getLogging().getLevel().name().toLowerCase());
                }
                var errorOutputStream = new ByteArrayOutputStream();
                build.setStandardError(errorOutputStream);

                var standardOutputStream = new ByteArrayOutputStream();
                build.setStandardOutput(standardOutputStream);

                var csvFile = getProject().getProjectDir().toPath().resolve(SKIPPY_DIRECTORY).resolve(test.getFullyQualifiedClassName() + ".csv");
                getLogger().lifecycle("Capturing coverage data for %s in %s".formatted(
                        test.getFullyQualifiedClassName(),
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
        });

    }
}
