package io.skippy.common.repository;

import io.skippy.common.model.TestImpactAnalysis;

public interface SkippyRepository {

    /**
     * Saves the {@link TestImpactAnalysis}
     *
     * @param testImpactAnalysis a {@link TestImpactAnalysis}
     * @return a 32-digit hex string that uniquely identifies the {@link TestImpactAnalysis}
     */
    String saveTestImpactAnalysis(TestImpactAnalysis testImpactAnalysis);

    /**
     * Saves JaCoCo execution data.
     *
     * @param jacocoExecutionData JaCoCo execution data
     * @return a 32-digit hex string that uniquely identifies the JaCoCo execution data
     */
    String saveJaCoCoExecutionData(byte[] jacocoExecutionData);
}
