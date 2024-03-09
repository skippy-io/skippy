package io.skippy.common.repository;

import io.skippy.common.model.TestImpactAnalysis;

import java.nio.file.Path;

/**
 * Repository for storage and retrieval of {@link TestImpactAnalysis} instances and Jacoco execution data.
 *
 * The default implementation stores both {@link TestImpactAnalysis} instances and Jacoco execution data in the
 * Skippy folder on the filesystem. In the future, users will be able to register custom implementations that use
 * alternative storage systems like databases or blob storage systems like AWS S3.
 */
public interface SkippyRepository {

    static SkippyRepository getInstance(Path projectDir) {
        return new DefaultSkippyRepository(projectDir);
    }

    /**
     * Saves the {@link TestImpactAnalysis}
     *
     * @param testImpactAnalysis a {@link TestImpactAnalysis}
     * @return a 32-digit hex string that uniquely identifies the {@link TestImpactAnalysis}
     */
    String saveTestImpactAnalysis(TestImpactAnalysis testImpactAnalysis);

    /**
     * Saves Jacoco execution data.
     *
     * @param jacocoExecutionData Jacoco execution data
     * @return a 32-digit hex string that uniquely identifies the Jacoco execution data
     */
    String saveJacocoExecutionData(byte[] jacocoExecutionData);

}
