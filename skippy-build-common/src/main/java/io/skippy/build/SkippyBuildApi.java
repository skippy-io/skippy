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
import io.skippy.common.model.*;
import io.skippy.common.repository.SkippyRepository;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.*;

import static io.skippy.common.SkippyConstants.*;
import static java.nio.file.Files.*;

/**
 * API that is used by Skippy's Gradle and Maven plugins to remoev the Skippy folder and to inform Skippy about events
 * like
 * <ul>
 *     <li>the start of a build,</li>
 *     <li>the end of a build and</li>
 *     <li>failed test cases.</li>
 * </ul>
 *
 * @author Florian McKee
 */
public final class SkippyBuildApi {

    private final Path projectDir;
    private final ClassFileCollector classFileCollector;
    private final SkippyRepository skippyRepository;
    private final Set<String> failedTests = new HashSet<>();


    /**
     * C'tor.
     *
     * @param projectDir         the project folder
     * @param classFileCollector the {@link ClassFileCollector}
     * @param skippyRepository   the {@link SkippyRepository}
     */
    public SkippyBuildApi(Path projectDir, ClassFileCollector classFileCollector, SkippyRepository skippyRepository) {
        this.projectDir = projectDir;
        this.classFileCollector = classFileCollector;
        this.skippyRepository = skippyRepository;
    }

    /**
     * Deletes the skippy folder.
     */
    public void deleteSkippyFolder() {
        try {
            var skippyFolder = SkippyFolder.get(projectDir);
            if (exists(skippyFolder)) {
                try (var stream = newDirectoryStream(skippyFolder)) {
                    for (Path file : stream) {
                        delete(file);
                    }
                }
                delete(skippyFolder);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to delete the Skippy folder: %s.".formatted(e), e);
        }
    }

    /**
     * Informs Skippy that a build has started.
     */
    public void buildStarted() {
        var predictionsLog = SkippyFolder.get(projectDir).resolve(PREDICTIONS_LOG_FILE);
        if (exists(predictionsLog)) {
            try {
                delete(predictionsLog);
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to delete %s: %s.".formatted(predictionsLog, e.getMessage()), e);
            }
        }
        var profilingLog = SkippyFolder.get(projectDir).resolve(PROFILING_LOG_FILE);
        if (exists(profilingLog)) {
            try {
                delete(profilingLog);
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to delete %s: %s.".formatted(profilingLog, e.getMessage()), e);
            }
        }
    }

    /**
     * Informs Skippy that a build has finished.
     *
     * @param createExecutionDataFileForSkippedTests Skippy will generate a JaCoCo execution data file for skipped tests if set to {@code true}
     */
    public void buildFinished(boolean createExecutionDataFileForSkippedTests) {
        upsert(failedTests, createExecutionDataFileForSkippedTests);
    }

    /**
     * Informs Skippy that a test has failed.
     *
     * @param className the class name of the failed tests
     */
    public void testFailed(String className) {
        failedTests.add(className);
    }

    private void upsert(Set<String> failedTests, boolean createExecutionDataFileForSkippedTests) {
        try {
            var existingAnalysis = skippyRepository.readTestImpactAnalysis();
            var newAnalysis = getTestImpactAnalysis(failedTests, createExecutionDataFileForSkippedTests);
            var mergedAnalysis = existingAnalysis.merge(newAnalysis);
            skippyRepository.saveTestImpactAnalysis(mergedAnalysis);
        } catch (IOException e) {
            throw new UncheckedIOException("Upsert failed: %s".formatted(e.getMessage()), e);
        }
    }

    private TestImpactAnalysis getTestImpactAnalysis(Set<String> failedTests, boolean createExecutionDataFileForSkippedTests) throws IOException {
        var classFileContainer = ClassFileContainer.from(classFileCollector.collect());
        var executionDataForCurrentBuild = skippyRepository.getTemporaryTestExecutionDataForCurrentBuild();
        var analyzedTests = executionDataForCurrentBuild.stream()
                .flatMap(testWithExecutionData -> getAnalyzedTests(failedTests, testWithExecutionData, classFileContainer, createExecutionDataFileForSkippedTests).stream())
                .toList();
        return new TestImpactAnalysis(classFileContainer, analyzedTests);
    }

    private List<AnalyzedTest> getAnalyzedTests(
            Set<String> failedTests,
            TestWithJacocoExecutionDataAndCoveredClasses testWithExecutionData,
            ClassFileContainer classFileContainer,
            boolean createExecutionDataFileForSkippedTests
    ) {
        var testResult = failedTests.contains(testWithExecutionData.testClassName()) ? TestResult.FAILED : TestResult.PASSED;
        var ids = classFileContainer.getIdsByClassName(testWithExecutionData.testClassName());
        if (createExecutionDataFileForSkippedTests) {
            var execution = skippyRepository.saveJacocoExecutionData(testWithExecutionData.jacocoExecutionData());
            return ids.stream()
                    .map(id -> new AnalyzedTest(id, testResult, getCoveredClassesIds(testWithExecutionData.coveredClasses(), classFileContainer), execution))
                    .toList();
        } else {
            return ids.stream()
                    .map(id -> new AnalyzedTest(id, testResult, getCoveredClassesIds(testWithExecutionData.coveredClasses(), classFileContainer)))
                    .toList();
        }
    }

    private List<String> getCoveredClassesIds(List<String> coveredClasses, ClassFileContainer classFileContainer) {
        var coveredClassIds = new LinkedList<String>();
        for (String clazz : coveredClasses) {
            coveredClassIds.addAll(classFileContainer.getIdsByClassName(clazz));
        }
        return coveredClassIds;
    }

}