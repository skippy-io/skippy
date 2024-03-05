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

package io.skippy.build;

import io.skippy.common.SkippyFolder;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static io.skippy.common.SkippyConstants.*;

/**
 * API with functionality that is used across build-tool specific libraries (e.g., skippy-gradle and skippy-maven).
 *
 * @author Florian McKee
 */
public final class SkippyBuildApi {

    private final Path projectDir;
    private final TestImpactAnalysisWriter testImpactAnalysisWriter;

    private final Set<String> failedTests = new HashSet<>();

    /**
     * C'tor.
     *
     * @param projectDir the project folder
     * @param classFileCollector the {@link ClassFileCollector}
     */
    public SkippyBuildApi(Path projectDir, ClassFileCollector classFileCollector) {
        this.projectDir = projectDir;
        this.testImpactAnalysisWriter = new TestImpactAnalysisWriter(projectDir, classFileCollector);
    }

    /**
     * Removes the skippy folder.
     */
    public void removeSkippyFolder() {
        var skippyFolder = SkippyFolder.get(projectDir).toFile();
        if (skippyFolder.exists()) {
            for (var file : skippyFolder.listFiles()) {
                file.delete();
            }
            skippyFolder.delete();
        }
    }

    /**
     * Informs Skippy that a build has started.
     */
    public void buildStarted() {
        var skippyFolder = SkippyFolder.get(projectDir);
        var predictionsLog = skippyFolder.resolve(PREDICTIONS_LOG_FILE).toFile();
        if (predictionsLog.exists()) {
            predictionsLog.delete();
        }
        var profilingLog = skippyFolder.resolve(PROFILING_LOG_FILE).toFile();
        if (profilingLog.exists()) {
            profilingLog.delete();
        }
    }

    /**
     * Informs Skippy that a build has finished.
     */
    public void buildFinished() {
        testImpactAnalysisWriter.upsert(failedTests);
    }

    /**
     * Informs Skippy that a test has failed.
     *
     * @param className the class name of the failed tests
     */
    public void testFailed(String className) {
        failedTests.add(className);
    }

}