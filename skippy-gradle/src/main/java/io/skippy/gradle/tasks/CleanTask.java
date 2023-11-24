package io.skippy.gradle.tasks;

import org.gradle.api.DefaultTask;
import javax.inject.Inject;

import static io.skippy.gradle.core.SkippyConstants.SKIPPY_DIRECTORY;

public class CleanTask extends DefaultTask {

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
