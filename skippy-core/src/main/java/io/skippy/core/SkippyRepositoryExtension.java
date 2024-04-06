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
 * Extension point that allows projects to customize retrieval and storage of {@link TestImpactAnalysis} instances and
 * JaCoCo execution data files.
 * <br /><br />
 * Custom implementations must have a public constructor that accepts a single argument of type {@link java.nio.file.Path}.
 * Skippy will pass the project directory when the instance is created.
 * <br /><br />
 * Custom implementations must be registered using Skippy's build plugins.
 * <br /><br />
 * Gradle example:
 * <pre>
 * skippy {
 *     ...
 *     repository = 'com.example.S3SkippyRepository'
 * }
 * </pre>
 *
 * @author Florian McKee
 */
public interface SkippyRepositoryExtension {

    /**
     * Retrieves a {@link TestImpactAnalysis} by {@code id}.
     *
     * @param id must not be null
     * @return the {@link TestImpactAnalysis} with the given {@code id} or {@link Optional#empty()} if none found
     */
    Optional<TestImpactAnalysis> findTestImpactAnalysis(String id);

    /**
     * Saves a given {@link TestImpactAnalysis}.
     *
     * @param testImpactAnalysis must not be null
     */
    void saveTestImpactAnalysis(TestImpactAnalysis testImpactAnalysis);


    /**
     * Retrieves JaCoCo execution data by {@code id}.
     *
     * @param executionId a unique identifier for the execution data
     * @return the JaCoCo execution data with the given {@code executionId} or {@link Optional#empty()} if none found
     */
    Optional<byte[]> findJacocoExecutionData(String  executionId);

    /**
     * Saves JaCoCo execution data.
     *
     * @param executionId a unique identifier for the execution data
     * @param jacocoExecutionData must not be null
     */
    void saveJacocoExecutionData(String executionId, byte[] jacocoExecutionData);
}