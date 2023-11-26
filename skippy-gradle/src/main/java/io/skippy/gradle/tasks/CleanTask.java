package io.skippy.gradle.tasks;

import org.gradle.api.DefaultTask;
import javax.inject.Inject;

import static io.skippy.gradle.core.SkippyConstants.SKIPPY_DIRECTORY;

/**
 * The tasks that is run via <code>./gradlew skippyAnalyze</code>.
 */
public class CleanTask extends DefaultTask {

    /**
     * C'tor.
     */
    @Inject
    public CleanTask() {
        setGroup("skippy");
        doLast((task) -> {
            var skippyDir = getProject().getProjectDir().toPath().resolve(SKIPPY_DIRECTORY);
            getProject().delete(skippyDir);
            getProject().mkdir(skippyDir);
        });
    }
}
