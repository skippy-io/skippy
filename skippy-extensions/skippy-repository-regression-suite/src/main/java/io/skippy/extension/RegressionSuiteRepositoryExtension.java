package io.skippy.extension;

import io.skippy.core.DefaultRepositoryExtension;
import io.skippy.core.SkippyRepositoryExtension;
import io.skippy.core.TestImpactAnalysis;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.writeString;

/**
 * Custom {@link SkippyRepositoryExtension} that is internally used by the tests in skippy-regression-suite.
 */
public class RegressionSuiteRepositoryExtension implements SkippyRepositoryExtension  {

    private final SkippyRepositoryExtension defaultExtension;

    /**
     * Constructor that will be invoked via reflection.
     *
     * @param projectDir the project directory (e.g., ~/repo)
     * @throws IOException an {@link IOException}
     */
    public RegressionSuiteRepositoryExtension(Path projectDir) throws IOException {
        defaultExtension = new DefaultRepositoryExtension(projectDir);
        // write a marker that will be checked by the regression tests
        createDirectories(projectDir.resolve(".skippy"));
        writeString(projectDir.resolve(".skippy").resolve("REPOSITORY"), getClass().getName(), StandardCharsets.UTF_8);
    }

    @Override
    public Optional<TestImpactAnalysis> findTestImpactAnalysis(String id) {
        return defaultExtension.findTestImpactAnalysis(id);
    }

    @Override
    public void saveTestImpactAnalysis(TestImpactAnalysis testImpactAnalysis) {
        defaultExtension.saveTestImpactAnalysis(testImpactAnalysis);
    }

    @Override
    public Optional<byte[]> findJacocoExecutionData(String testExecutionId) {
        return defaultExtension.findJacocoExecutionData(testExecutionId);
    }

    @Override
    public void saveJacocoExecutionData(String testExecutionId, byte[] jacocoExecutionData) {
        defaultExtension.saveJacocoExecutionData(testExecutionId, jacocoExecutionData);
    }
}
