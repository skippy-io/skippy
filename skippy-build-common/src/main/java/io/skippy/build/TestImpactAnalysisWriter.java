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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static io.skippy.common.SkippyConstants.TEST_IMPACT_ANALYSIS_JSON_FILE;
import static java.util.Arrays.asList;

/**
 * @author Florian McKee
 */
final class TestImpactAnalysisWriter {

    private final Path projectDir;
    private final ClassFileCollector classFileCollector;

    TestImpactAnalysisWriter(Path projectDir, ClassFileCollector classFileCollector) {
        this.projectDir = projectDir;
        this.classFileCollector = classFileCollector;
    }

    void upsert(Set<String> failedTests) {
        try {
            var covFiles = asList(SkippyFolder.get(projectDir).toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".cov")));
            var existingAnalysis = TestImpactAnalysis.readFromFile(SkippyFolder.get(projectDir).resolve(TEST_IMPACT_ANALYSIS_JSON_FILE));
            var newAnalysis = getTestImpactAnalysis(failedTests, covFiles);
            var mergedAnalysis = existingAnalysis.merge(newAnalysis);
            Files.writeString(SkippyFolder.get(projectDir).resolve(TEST_IMPACT_ANALYSIS_JSON_FILE), mergedAnalysis.toJson(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            covFiles.stream().forEach(File::delete);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private TestImpactAnalysis getTestImpactAnalysis(Set<String> failedTests, List<File> covFiles) throws IOException {
        var classFileContainer = ClassFileContainer.from(classFileCollector.collect());
        var analyzedTests = covFiles.stream().flatMap(covFile -> getAnalyzedTests(failedTests, covFile, classFileContainer).stream()).toList();
        return new TestImpactAnalysis(classFileContainer, analyzedTests);
    }

    private List<AnalyzedTest> getAnalyzedTests(Set<String> failedTests, File covFile, ClassFileContainer classFileContainer) {
        var testName = covFile.getName().substring(0, covFile.getName().indexOf(".cov"));
        var testResult = failedTests.contains(testName) ? TestResult.FAILED : TestResult.PASSED;
        return classFileContainer.getIdsByClassName(testName).stream()
                .map(id -> new AnalyzedTest(id, testResult, getCoveredClasses(covFile, classFileContainer)))
                .toList();
    }

    private static List<String> getCoveredClasses(File covFile, ClassFileContainer classFileContainer) {
        try {
            List<String> coveredClasses = new LinkedList<>();
            for (String clazz : Files.readAllLines(covFile.toPath(), StandardCharsets.UTF_8)) {
                coveredClasses.addAll(classFileContainer.getIdsByClassName(clazz.replace("/", ".").trim()));
            }
            return coveredClasses;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}