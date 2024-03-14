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

import io.skippy.common.model.*;
import io.skippy.common.repository.SkippyRepository;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.*;


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

    private final ClassFileCollector classFileCollector;
    private final SkippyRepository skippyRepository;
    private final Set<String> failedTests = new HashSet<>();

    /**
     * C'tor.
     *
     * @param classFileCollector the {@link ClassFileCollector}
     * @param skippyRepository   the {@link SkippyRepository}
     */
    public SkippyBuildApi(ClassFileCollector classFileCollector, SkippyRepository skippyRepository) {
        this.classFileCollector = classFileCollector;
        this.skippyRepository = skippyRepository;
    }

    /**
     * Deletes the skippy folder.
     */
    public void deleteSkippyFolder() {
        skippyRepository.deleteSkippyFolder();
    }

    /**
     * Informs Skippy that a build has started.
     *
     * @param skippyConfiguration the {@link SkippyConfiguration}
     */
    public void buildStarted(SkippyConfiguration skippyConfiguration) {
        skippyRepository.deleteLogFiles();
        skippyRepository.saveConfiguration(skippyConfiguration);
    }

    /**
     * Informs Skippy that a build has finished.
     *
     * @param skippyConfiguration the {@link SkippyConfiguration}
     */
    public void buildFinished(SkippyConfiguration skippyConfiguration) {
        upsert(failedTests, skippyConfiguration);
    }

    /**
     * Informs Skippy that a test has failed.
     *
     * @param className the class name of the failed tests
     */
    public void testFailed(String className) {
        failedTests.add(className);
    }

    private void upsert(Set<String> failedTests, SkippyConfiguration skippyConfiguration) {
        try {
            var existingAnalysis = skippyRepository.readTestImpactAnalysis();
            var newAnalysis = getTestImpactAnalysis(failedTests, skippyConfiguration.persistExecutionData());
            var mergedAnalysis = existingAnalysis.merge(newAnalysis);
            skippyRepository.saveTestImpactAnalysis(mergedAnalysis);
        } catch (IOException e) {
            throw new UncheckedIOException("Upsert failed: %s".formatted(e.getMessage()), e);
        }
    }

    private TestImpactAnalysis getTestImpactAnalysis(Set<String> failedTests, boolean persistExecutionData) throws IOException {
        var classFileContainer = ClassFileContainer.from(classFileCollector.collect());
        var executionDataForCurrentBuild = skippyRepository.getTemporaryTestExecutionDataForCurrentBuild();
        var analyzedTests = executionDataForCurrentBuild.stream()
                .flatMap(testWithExecutionData -> getAnalyzedTests(failedTests, testWithExecutionData, classFileContainer, persistExecutionData).stream())
                .toList();
        return new TestImpactAnalysis(classFileContainer, analyzedTests);
    }

    private List<AnalyzedTest> getAnalyzedTests(
            Set<String> failedTests,
            TestWithJacocoExecutionDataAndCoveredClasses testWithExecutionData,
            ClassFileContainer classFileContainer,
            boolean persistExecutionData
    ) {
        var testResult = failedTests.contains(testWithExecutionData.testClassName()) ? TestResult.FAILED : TestResult.PASSED;
        var ids = classFileContainer.getIdsByClassName(testWithExecutionData.testClassName());
        if (persistExecutionData) {
            var executionId = skippyRepository.saveJacocoExecutionData(testWithExecutionData.jacocoExecutionData());
            return ids.stream()
                    .map(id -> new AnalyzedTest(id, testResult, getCoveredClassesIds(testWithExecutionData.coveredClasses(), classFileContainer), executionId))
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