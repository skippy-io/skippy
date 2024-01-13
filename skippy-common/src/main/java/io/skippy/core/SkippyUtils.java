package io.skippy.core;

import java.nio.file.Path;

import static io.skippy.core.SkippyConstants.SKIPPY_DIRECTORY;

public class SkippyUtils {

    public static Path getOrCreateSkippyFolder() {
        return getOrCreateSkippyFolder(Path.of("."));
    }

    public static Path getOrCreateSkippyFolder(Path projectDir) {
        var skippyFolder = getSkippyFolder(projectDir);
        if ( ! skippyFolder.toFile().exists()) {
            skippyFolder.toFile().mkdirs();
        }
        return skippyFolder;
    }

    public static Path getSkippyFolder(Path projectDir) {
        return projectDir.resolve(SKIPPY_DIRECTORY);
    }

    public static Path getSkippyFolder() {
        return SKIPPY_DIRECTORY;
    }
}
