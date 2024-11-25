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
import java.net.URL;
import java.net.URLClassLoader;

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
     * Resets the skippy folder.
     */
    public void resetSkippyFolder() {
        skippyRepository.resetSkippyFolder();
        skippyRepository.saveConfiguration(skippyConfiguration);
    }

    /**
     * Informs Skippy that a build has started.
     */
    public void buildStarted() {
        skippyRepository.deleteLogFiles();
        skippyRepository.deleteTmpFolder();
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

    private TestImpactAnalysis getTestImpactAnalysis() {
        var classFileContainer = ClassFileContainer.from(classFileCollector.collect());
        var testRecordings = skippyRepository.getTestRecordings();
        var analyzedTests = testRecordings.stream()
                .map(testWithExecutionData -> getAnalyzedTests(testWithExecutionData, classFileContainer))
                .toList();
        return new TestImpactAnalysis(classFileContainer, analyzedTests);
    }

    private AnalyzedTest getAnalyzedTests(
            TestRecording testRecording,
            ClassFileContainer classFileContainer
    ) {
        var classFile = classFileContainer.getClassFileFor(testRecording);
        var executionId = skippyConfiguration.generateCoverageForSkippedTests() ?
                Optional.of(skippyRepository.saveJacocoExecutionData(testRecording.jacocoExecutionData())) :
                Optional.<String>empty();
        return AnalyzedTest.from(classFileContainer, classFile, testRecording.tags(), getCoveredClasses(testRecording, classFileContainer), executionId);
    }

    private List<ClassFile> getCoveredClasses(TestRecording testRecording, ClassFileContainer classFileContainer) {
        var result = new LinkedList<ClassFile>();
        for (var coveredClass : testRecording.coveredClasses()) {
            result.addAll(classFileContainer.getClassFilesMatching(coveredClass));
        }
        return result;
    }

    public List<ClassFile> getExclusions() {
        var tia = skippyRepository.readLatestTestImpactAnalysis();
        return classFileCollector.collect().stream()
                .filter(classFile -> {
                    var prediction = tia.predict(toClass(classFile), skippyConfiguration, skippyRepository);
                    return prediction.prediction() == Prediction.SKIP;
                })
                .toList();
    }

    private Class<?> toClass(ClassFile classFile) {
        try {
            var fullyQualifiedOutputFolder = classFile.getFullyQualifiedPath();
            var outputFolder = fullyQualifiedOutputFolder;
            var dotsInClassName = classFile.getClassName().chars().filter(ch -> ch == '.').count();
            for (int i = 0; i <= dotsInClassName; i++) {
                outputFolder = outputFolder.getParent();
            }
            var url = outputFolder.toUri().toURL();
            try (var classLoader = new URLClassLoader(new URL[] { url } )) {
                return classLoader.loadClass(classFile.getClassName());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void beforeTest(Class<?> testClass) {
        SkippyTestApi.INSTANCE.before(testClass, null);
    }

    public void afterTest(Class<?> testClass) {
        SkippyTestApi.INSTANCE.after(testClass, null);
    }
}