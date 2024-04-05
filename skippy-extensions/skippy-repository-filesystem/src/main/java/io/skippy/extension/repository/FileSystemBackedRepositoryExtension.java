package io.skippy.extension.repository;

import io.skippy.core.SkippyRepositoryExtension;
import io.skippy.core.TestImpactAnalysis;

import java.nio.file.Path;
import java.util.Optional;

public class FileSystemBackedRepositoryExtension implements SkippyRepositoryExtension  {

    public FileSystemBackedRepositoryExtension(Path projectDir) {
    }

    @Override
    public Optional<TestImpactAnalysis> findTestImpactAnalysis(String id) {
        return Optional.empty();
    }

    @Override
    public void saveTestImpactAnalysis(TestImpactAnalysis testImpactAnalysis) {

    }

    @Override
    public Optional<byte[]> findJacocoExecutionData(String testExecutionId) {
        return Optional.empty();
    }

    @Override
    public String saveJacocoExecutionData(byte[] jacocoExecutionData) {
        return null;
    }
}
