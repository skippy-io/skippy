package io.skippy.common.repository;

import java.nio.file.Path;

public final class SkippyRepositoryFactory {

    public static SkippyRepository getSkippyRepository(Path projectDir) {
        return new DefaultSkippyRepository(projectDir);
    }

}