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

package io.skippy.common.model;

import io.skippy.common.SkippyFolder;
import io.skippy.common.util.Profiler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static io.skippy.common.SkippyConstants.TEST_IMPACT_ANALYSIS_JSON_FILE;
import static io.skippy.common.model.Reason.Category.*;
import static java.lang.System.lineSeparator;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;

/**
 * Programmatic representation of the `test-impact-analysis.json` file.
 *
 * @author Florian McKee
 */
public final class TestImpactAnalysis {

    public static final TestImpactAnalysis NOT_FOUND = new TestImpactAnalysis(ClassFileContainer.from(emptyList()), emptyList());
    private final ClassFileContainer classFileContainer;
    private final List<AnalyzedTest> analyzedTests;

    /**
     * Creates a new instance.
     *
     * @param classFileContainer a {@link ClassFileContainer}
     * @param analyzedTests a list of {@link AnalyzedTest}s
     */
    public TestImpactAnalysis(ClassFileContainer classFileContainer, List<AnalyzedTest> analyzedTests) {
        this.classFileContainer = classFileContainer;
        this.analyzedTests = analyzedTests;
    }

    /**
     * Creates a new instance based off the {@code testImpactAnalysisJsonFile}.
     *
     * @param testImpactAnalysisJsonFile JSON file that contains the Test Impact Analysis
     * @return a new instance based off the {@code testImpactAnalysisJsonFile}
     */
    public static TestImpactAnalysis readFromFile(Path testImpactAnalysisJsonFile) {
        if ( ! testImpactAnalysisJsonFile.toFile().exists()) {
            return NOT_FOUND;
        }
        try {
            return parse(Files.readString(testImpactAnalysisJsonFile, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new instance based off the JSON file in the Skippy folder.
     *
     * @return a new instance based off the JSON file in the Skippy folder
     */
    public static TestImpactAnalysis readFromSkippyFolder() {
        var testImpactAnalysisJsonFile = SkippyFolder.get().resolve(TEST_IMPACT_ANALYSIS_JSON_FILE);
        return readFromFile(testImpactAnalysisJsonFile);
    }

    /**
     * Makes a skip-or-execute prediction for the test identified by the {@code testClassName}.
     *
     * @param testClassName the test's fully-qualified class name (e.g., com.example.FooTest)
     * @return a skip-or-execute prediction for the test identified by the {@code testClassName}
     */
    public PredictionWithReason predict(String testClassName) {
        return Profiler.profile("TestImpactAnalysis#predict", () -> {
            var maybeAnalyzedTest = analyzedTests.stream()
                    .filter(test -> classFileContainer.getById(test.testClassId()).getClassName().equals(testClassName))
                    .findFirst();
            if (maybeAnalyzedTest.isEmpty()) {
                return PredictionWithReason.execute(new Reason(NO_DATA_FOUND_FOR_TEST, Optional.empty()));
            }
            var analyzedTest = maybeAnalyzedTest.get();
            var testClass = classFileContainer.getById(analyzedTest.testClassId());

            if (analyzedTest.result() == TestResult.FAILED) {
                return PredictionWithReason.execute(new Reason(TEST_FAILED_PREVIOUSLY, Optional.empty()));
            }

            if (testClass.classFileNotFound()) {
                return PredictionWithReason.execute(new Reason(TEST_CLASS_CLASS_FILE_NOT_FOUND, Optional.of(testClass.getClassFile().toString())));
            }

            if (testClass.hasChanged()) {
                return PredictionWithReason.execute(new Reason(BYTECODE_CHANGE_IN_TEST, Optional.empty()));
            }
            for (var coveredClassId : analyzedTest.coveredClassesIds()) {
                var coveredClass = classFileContainer.getById(coveredClassId);
                if (coveredClass.classFileNotFound()) {
                    return PredictionWithReason.execute(new Reason(COVERED_CLASS_CLASS_FILE_NOT_FOUND, Optional.of(coveredClass.getClassFile().toString())));
                }
                if (coveredClass.hasChanged()) {
                    return PredictionWithReason.execute(new Reason(BYTECODE_CHANGE_IN_COVERED_CLASS, Optional.of(coveredClass.getClassName())));
                }
            }
            return PredictionWithReason.skip(new Reason(NO_CHANGE, Optional.empty()));
        });
    }

    /**
     * Returns the Jacoco execution data references from the {@link AnalyzedTest}s.
     *
     * @return the Jacoco execution data references from the {@link AnalyzedTest}s
     */
    public List<String> getJacocoExecutionDataRefs() {
        return analyzedTests.stream().map(AnalyzedTest::jacocoExecutionDataRef).toList();
    }

    List<AnalyzedTest> getAnalyzedTests() {
        return analyzedTests;
    }

    ClassFileContainer getClassFileContainer() {
        return classFileContainer;
    }

    public static TestImpactAnalysis parse(String string) {
        return Profiler.profile("TestImpactAnalysis#parse", () -> parse(new Tokenizer(string)));
    }

    private static TestImpactAnalysis parse(Tokenizer tokenizer) {
        tokenizer.skip('{');
        ClassFileContainer classFileContainer = null;
        List<AnalyzedTest> analyzedTests = null;
        while (classFileContainer == null || analyzedTests == null) {
            var key = tokenizer.next();
            tokenizer.skip(':');
            switch (key) {
                case "classes":
                    classFileContainer = ClassFileContainer.parse(tokenizer);
                    break;
                case "tests":
                    analyzedTests = AnalyzedTest.parseList(tokenizer);
                    break;
            }
            if (classFileContainer == null || analyzedTests == null) {
                tokenizer.skip(',');
            }
        }
        tokenizer.skip('}');
        return new TestImpactAnalysis(classFileContainer, analyzedTests);
    }

    /**
     * Renders this instance as JSON string.
     *
     * @return the instance as JSON string
     */
    public String toJson() {
        return toJson(JsonProperty.values());
    }

    /**
     * Renders this instance as JSON string.
     *
     * @param propertiesToRender the properties to include in the JSON string
     * @return this instance as JSON string
     */
    public String toJson(JsonProperty... propertiesToRender) {
        return """
            {
                "classes": %s,
                "tests": [
            %s
                ]
            }""".formatted(
                classFileContainer.toJson(propertiesToRender),
                analyzedTests.stream().sorted().map(c -> c.toJson()).collect(joining("," + lineSeparator())
            )
        );
    }

    /**
     * Merges two {@link TestImpactAnalysis} instances to support incremental updates of the data in the Skippy folder.
     *
     * @param other a {@link TestImpactAnalysis} instance that will be merged with this instance
     * @return a new instance that represents the merge of this and the {@code other} instance
     */
    public TestImpactAnalysis merge(TestImpactAnalysis other) {
        return Profiler.profile("TestImpactAnalysis#merge", () -> {
            var mergedClassFileContainer = classFileContainer.merge(other.getClassFileContainer());
            var remappedTests = new TreeSet<AnalyzedTest>();
            for (var analyzedTest : other.analyzedTests) {
                var remappedTest = remap(analyzedTest, other.classFileContainer, mergedClassFileContainer);
                remappedTests.add(remappedTest);
            }
            for (var analyzedTest : this.analyzedTests) {
                var remappedTest = remap(analyzedTest, this.classFileContainer, mergedClassFileContainer);
                if (false == remappedTests.contains(remappedTest)) {
                    remappedTests.add(remappedTest);
                }
            }
            return new TestImpactAnalysis(mergedClassFileContainer, new ArrayList<>(remappedTests));
        });
    }

    private AnalyzedTest remap(AnalyzedTest analyzedTest, ClassFileContainer original, ClassFileContainer merged) {
        return new AnalyzedTest(
                remap(analyzedTest.testClassId(), original, merged),
                analyzedTest.result(),
                analyzedTest.coveredClassesIds().stream().map(id -> remap(id, original, merged)).toList(),
                analyzedTest.jacocoExecutionDataRef());
    }

    private String remap(String id, ClassFileContainer original, ClassFileContainer merged) {
        var classFile = original.getById(id);
        return merged.getId(classFile);
    }
}