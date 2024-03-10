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

package io.skippy.common.repository;

import io.skippy.common.model.TestWithJacocoExecutionDataAndCoveredClasses;
import io.skippy.common.model.TestImpactAnalysis;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Repository for
 * <ul>
 *     <li>storage and retrieval of {@link TestImpactAnalysis} instances and Jacoco execution data and</li>
 *     <li>storage of retrieval of temporary files that are used for communication between Skippy's JUnit and build libraries.</li>
 * </ul>
 *
 * The default implementation stores both {@link TestImpactAnalysis} instances and Jacoco execution data in the
 * Skippy folder on the filesystem. In the future, users will be able to register custom implementations that use
 * alternative storage systems like databases or blob storage systems like AWS S3.
 *
 * @author Florian McKee
 */
public interface SkippyRepository {

    static SkippyRepository getInstance(Path projectDir) {
        return new DefaultSkippyRepository(projectDir);
    }

    static SkippyRepository getInstance() {
        return new DefaultSkippyRepository(Path.of("."));
    }

    /**
     * Reads the {@link TestImpactAnalysis} instance for a project.
     *
     * @return an {@link Optional} with the instance if one was found, an empty {@link Optional} otherwise
     */
    TestImpactAnalysis readTestImpactAnalysis();

    /**
     * Saves the {@link TestImpactAnalysis}.
     *
     * @param testImpactAnalysis a {@link TestImpactAnalysis}
     */
    void saveTestImpactAnalysis(TestImpactAnalysis testImpactAnalysis);

    /**
     * Saves Jacoco execution data.
     *
     * @param jacocoExecutionData Jacoco execution data
     * @return a identifier that uniquely identifies the Jacoco execution data
     */
    String saveJacocoExecutionData(byte[] jacocoExecutionData);

    /**
     * Allows Skippy's JUnit libraries to temporarily save test execution data. The data will be automatically deleted
     * after the build finishes.
     *
     * @param testClassName the name of a test class (e.g., com.example.FooTest)
     * @param jacocoExecutionData Jacoco execution data for the test.
     */
    void saveTemporaryTestExecutionDataForCurrentBuild(String testClassName, byte[] jacocoExecutionData);

    /**
     * Returns the test execution data for the current build that has been generated by Skippy's JUnit libraries.
     *
     * @return the test execution data for the current build that has been generated by Skippy's JUnit libraries
     */
    List<TestWithJacocoExecutionDataAndCoveredClasses> getTemporaryTestExecutionDataForCurrentBuild();

}