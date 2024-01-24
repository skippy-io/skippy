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
import io.skippy.common.model.AnalyzedTest;
import io.skippy.common.model.TestImpactAnalysis;
import io.skippy.common.model.TestResult;
import io.skippy.common.model.ClassFile;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

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

    void upsert() {
        try {
            var covFiles = asList(SkippyFolder.get(projectDir).toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".cov")));
            var testImpactAnalysis = getTestImpactAnalysis(covFiles);
            Files.writeString(SkippyFolder.get(projectDir).resolve( TEST_IMPACT_ANALYSIS_JSON_FILE), testImpactAnalysis.toJson(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            covFiles.stream().forEach(File::delete);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private TestImpactAnalysis getTestImpactAnalysis(List<File> covFiles) throws IOException {
        Map<String, ClassFile> classFiles = classFileCollector.collect().stream()
                // this implementation currently ignores duplicate class names across output folders
                .collect(Collectors.toMap(ClassFile::getClassName, it -> it, (first, second) -> first));
        var skippifiedTests = covFiles.stream().map(covFile -> getSkippifiedTest(covFile, classFiles)).toList();
        return new TestImpactAnalysis(skippifiedTests);
    }

    private AnalyzedTest getSkippifiedTest(File covFile, Map<String, ClassFile> classFiles) {
        var testName = covFile.getName().substring(0, covFile.getName().indexOf(".cov"));
        return new AnalyzedTest(classFiles.get(testName), TestResult.SUCCESS, getCoveredClasses(covFile, classFiles));
    }

    private static List<ClassFile> getCoveredClasses(File covFile, Map<String, ClassFile> classFiles) {
        try {
            List<ClassFile> coveredClasses = new LinkedList<>();
            for (String clazz : Files.readAllLines(covFile.toPath(), StandardCharsets.UTF_8)) {
                var classFile = classFiles.get(clazz.replace("/", "."));
                if (classFile != null) {
                    coveredClasses.add(classFile);
                }
            }
            return coveredClasses;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}