package io.skippy.gradle.tasks;

import io.skippy.gradle.core.AnalyzedFile;
import org.gradle.api.DefaultTask;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;

import static io.skippy.gradle.core.SkippyConstants.SKIPPY_DIRECTORY;
import static java.util.Arrays.asList;

/**
 * Task that performs a JaCoCo coverage analysis for a skippified tests.
 *
 * <code>./gradlew skippyAnalyze</code> automatically executes tasks of this type 'under the hood'. It is not meant
 * to be executed directly.
 *
 * <p>Example:</p>
 *
 * <pre>
 * ./gradlew tasks
 *
 * ...
 *
 * Skippy (internal) tasks
 * -----------------------
 * skippyCoverage_com.example.SkippifiedTest1
 * skippyCoverage_com.example.SkippifiedTest2
 * </pre>
 */
public class CoverageTask extends DefaultTask {

    /**
     * C'tor.
     *
     * @param skippifiedTest {@link AnalyzedFile} representing a skippified test
     */
    @Inject
    public CoverageTask(AnalyzedFile skippifiedTest) {
        setGroup("skippy (internal)");
        setMustRunAfter(asList("skippyClean"));

        doLast(task -> {
            GradleConnector connector = GradleConnector.newConnector();
            connector.forProjectDirectory(getProject().getProjectDir());
            try (ProjectConnection connection = connector.connect()) {
                BuildLauncher build = connection.newBuild();
                build.forTasks("test", "jacocoTestReport");
                build.addArguments("-PskippyCoverageBuild=" + skippifiedTest.getFullyQualifiedClassName());
                if (getLogging().getLevel() != null) {
                    build.addArguments("--" + getLogging().getLevel().name().toLowerCase());
                }
                var errorOutputStream = new ByteArrayOutputStream();
                build.setStandardError(errorOutputStream);

                var standardOutputStream = new ByteArrayOutputStream();
                build.setStandardOutput(standardOutputStream);

                var csvFile = getProject().getProjectDir().toPath().resolve(SKIPPY_DIRECTORY).resolve(skippifiedTest.getFullyQualifiedClassName() + ".csv");
                getLogger().lifecycle("Capturing coverage data for %s in %s".formatted(
                        skippifiedTest.getFullyQualifiedClassName(),
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
