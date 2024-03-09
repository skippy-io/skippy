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
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.SessionInfoStore;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static io.skippy.common.SkippyConstants.*;
import static java.util.Arrays.asList;

/**
 * API that is used by Skippy's Gradle and Maven plugins.
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
     * @param projectDir the project folder
     * @param classFileCollector the {@link ClassFileCollector}
     * @param skippyRepository the {@link SkippyRepository}
     */
    public SkippyBuildApi(Path projectDir, ClassFileCollector classFileCollector, SkippyRepository skippyRepository) {
        this.projectDir = projectDir;
        this.classFileCollector = classFileCollector;
        this.skippyRepository = skippyRepository;
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
        upsert(failedTests);
    }

    /**
     * Informs Skippy that a test has failed.
     *
     * @param className the class name of the failed tests
     */
    public void testFailed(String className) {
        failedTests.add(className);
    }

    private void upsert(Set<String> failedTests) {
        try {
            var execFiles = asList(SkippyFolder.get(projectDir).toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".exec")));
            var existingAnalysis = TestImpactAnalysis.readFromFile(SkippyFolder.get(projectDir).resolve(TEST_IMPACT_ANALYSIS_JSON_FILE));
            var newAnalysis = getTestImpactAnalysis(failedTests, execFiles);
            var mergedAnalysis = existingAnalysis.merge(newAnalysis);
            skippyRepository.saveTestImpactAnalysis(mergedAnalysis);
            execFiles.stream().forEach(File::delete);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private TestImpactAnalysis getTestImpactAnalysis(Set<String> failedTests, List<File> execFiles) throws IOException {
        var classFileContainer = ClassFileContainer.from(classFileCollector.collect());
        var analyzedTests = execFiles.stream().flatMap(execFile -> getAnalyzedTests(failedTests, execFile, classFileContainer).stream()).toList();
        return new TestImpactAnalysis(classFileContainer, analyzedTests);
    }

    private List<AnalyzedTest> getAnalyzedTests(Set<String> failedTests, File execFile, ClassFileContainer classFileContainer) {
        var executionDataRef = Optional.of(getExecutionDataRef(execFile));
        var testName = execFile.getName().substring(0, execFile.getName().indexOf(".coverage"));
        var testResult = failedTests.contains(testName) ? TestResult.FAILED : TestResult.PASSED;
        return classFileContainer.getIdsByClassName(testName).stream()
                .map(id -> new AnalyzedTest(id, testResult, getCoveredClassesIds(execFile, classFileContainer), executionDataRef))
                .toList();
    }

    private String getExecutionDataRef(File execFile) {
        try {
            return skippyRepository.saveJaCoCoExecutionData(Files.readAllBytes(execFile.toPath()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<String> getCoveredClassesIds(File execFile, ClassFileContainer classFileContainer) {
        try {
            var coveredClasses = new LinkedList<String>();
            ExecutionDataReader executionDataReader = new ExecutionDataReader(new ByteArrayInputStream(Files.readAllBytes(execFile.toPath())));
            executionDataReader.setSessionInfoVisitor(new SessionInfoStore());
            executionDataReader.setExecutionDataVisitor(visitor -> coveredClasses.add(visitor.getName()));
            executionDataReader.read();
            var coveredClassIds = new LinkedList<String>();
            for (String clazz : coveredClasses) {
                coveredClassIds.addAll(classFileContainer.getIdsByClassName(clazz.replace("/", ".").trim()));
            }
            return coveredClasses;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}