/*
 * Copyright 2023-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.skippy.core;

import java.util.Optional;

/**
 * Extension point that allows projects to customize Skippy's default implementation for
 * <ul>
 *     <li>{@link SkippyRepository#readTestImpactAnalysis()},</li>
 *     <li>{@link SkippyRepository#saveTestImpactAnalysis(TestImpactAnalysis)},</li>
 *     <li>{@link SkippyRepository#readJacocoExecutionData(String)} and </li>
 *     <li>{@link SkippyRepository#saveJacocoExecutionData}</li>
 * </ul>
 *
 * See {@link SkippyRepository} for more information.
 *
 * @author Florian McKee
 */
public interface SkippyRepositoryExtension {

    /**
     * Returns the {@link TestImpactAnalysis} instance for the current build or an empty {@link Optional} is none
     * was found.
     *
     * @return the {@link TestImpactAnalysis} instance for the current build or an empty {@link Optional} is none
     *      was found
     */
    Optional<TestImpactAnalysis> readTestImpactAnalysis();

    /**
     * Saves the {@link TestImpactAnalysis} generated by the current build.
     *
     * @param testImpactAnalysis a {@link TestImpactAnalysis}
     */
    void saveTestImpactAnalysis(TestImpactAnalysis testImpactAnalysis);


    /**
     * Returns the Jacoco execution data for the given {@code executionId} or an empty {@link Optional} if none was found.
     *
     * @param executionId the unique identifier for the execution data that was returned by {@link SkippyRepositoryExtension#saveJacocoExecutionData(byte[])}
     * @return Jacoco execution data for the given {@code executionId} or an empty {@link Optional} if none was found
     */
    Optional<byte[]> readJacocoExecutionData(String executionId);

    /**
     * Saves Jacoco execution data for usage by subsequent builds.
     *
     * @param jacocoExecutionData Jacoco execution data
     * @return a unique identifier for the execution data (also referred to as execution id)
     */
    String saveJacocoExecutionData(byte[] jacocoExecutionData);
}