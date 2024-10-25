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

import java.util.*;

/**
 * API that is used by Skippy's Gradle and Maven plugins to remove the Skippy folder and to inform Skippy about events
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

    private final SkippyConfiguration skippyConfiguration;
    private final ClassFileCollector classFileCollector;
    private final SkippyRepository skippyRepository;

    /**
     * C'tor.
     *
     * @param skippyConfiguration the {@link SkippyConfiguration}
     * @param classFileCollector  the {@link ClassFileCollector}
     * @param skippyRepository    the {@link SkippyRepository}
     */
    public SkippyBuildApi(SkippyConfiguration skippyConfiguration, ClassFileCollector classFileCollector, SkippyRepository skippyRepository) {
        this.skippyConfiguration = skippyConfiguration;
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
     */
    public void buildStarted() {
        skippyRepository.deleteLogFiles();
        skippyRepository.deleteListOfFailedTests();
        skippyRepository.saveConfiguration(skippyConfiguration);
    }

    /**
     * Informs Skippy that a build has finished.
     */
    public void buildFinished() {
        var existingAnalysis = skippyRepository.readLatestTestImpactAnalysis();
        var newAnalysis = getTestImpactAnalysis();
        var mergedAnalysis = existingAnalysis.merge(newAnalysis);
        skippyRepository.saveTestImpactAnalysis(mergedAnalysis);
        if (skippyConfiguration.generateCoverageForSkippedTests()) {
            generateCoverageForSkippedTests(mergedAnalysis);
        }
    }

    private void generateCoverageForSkippedTests(TestImpactAnalysis testImpactAnalysis) {
        var skippedTestClassNames = skippyRepository.readPredictionsLog().stream()
                .filter(classNameAndPrediction -> classNameAndPrediction.prediction() == Prediction.SKIP)
                .map(classNameAndPrediction -> classNameAndPrediction.className())
                .toList();

        var skippedTests = testImpactAnalysis.getAnalyzedTests().stream()
                .filter(test -> skippedTestClassNames.contains(testImpactAnalysis.getClassFileContainer().getById(test.getTestClassId()).getClassName()))
                .toList();

        List<byte[]> executionData = skippedTests.stream()
                .flatMap(skippedTest -> skippyRepository.readJacocoExecutionData(skippedTest.getExecutionId().get()).stream())
                .toList();
        byte[] mergeExecutionData = JacocoUtil.mergeExecutionData(executionData);

        skippyRepository.saveExecutionDataForSkippedTests(mergeExecutionData);
    }

    /**
     * Informs Skippy that a test has failed.
     *
     * @param className the class name of the failed tests
     */
    public void testFailed(String className) {
        skippyRepository.recordFailedTest(className);
    }

    private TestImpactAnalysis getTestImpactAnalysis() {
        var classFileContainer = ClassFileContainer.from(classFileCollector.collect());
        var executionDataForCurrentBuild = skippyRepository.readTemporaryJaCoCoExecutionDataForCurrentBuild();
        var analyzedTests = executionDataForCurrentBuild.stream()
                .flatMap(testWithExecutionData -> getAnalyzedTests(testWithExecutionData, classFileContainer).stream())
                .toList();
        return new TestImpactAnalysis(classFileContainer, analyzedTests);
    }

    private List<AnalyzedTest> getAnalyzedTests(
            TestWithJacocoExecutionDataAndCoveredClasses testWithExecutionData,
            ClassFileContainer classFileContainer
    ) {
        var failedTests = skippyRepository.readListOfFailedTests();
        var testResult = failedTests.contains(testWithExecutionData.testClassName()) ? TestResult.FAILED : TestResult.PASSED;
        var ids = classFileContainer.getIdsByClassName(testWithExecutionData.testClassName());
        var executionId = skippyConfiguration.generateCoverageForSkippedTests() ?
                Optional.of(skippyRepository.saveJacocoExecutionData(testWithExecutionData.jacocoExecutionData())) :
                Optional.<String>empty();
        return ids.stream()
                .map(id -> new AnalyzedTest(id, testResult, getCoveredClassesIds(testWithExecutionData.coveredClasses(), classFileContainer), executionId))
                .toList();
    }

    private List<Integer> getCoveredClassesIds(List<String> coveredClasses, ClassFileContainer classFileContainer) {
        var coveredClassIds = new LinkedList<Integer>();
        for (String clazz : coveredClasses) {
            coveredClassIds.addAll(classFileContainer.getIdsByClassName(clazz));
        }
        return coveredClassIds;
    }

}