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

import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.skippy.core.Reason.Category.*;
import static io.skippy.core.HashUtil.hashWith32Digits;
import static java.lang.System.lineSeparator;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;

/**
 * The data and logic that allows Skippy to make test-or-execute predictions.
 * <br /><br />
 * A {@link TestImpactAnalysis} is made up of
 * <ul>
 *     <li>a {@link ClassFileContainer} and</li>
 *     <li>a list of {@link AnalyzedTest}s</li>
 * </ul>
 * The {@link ClassFileContainer} contains static information for every class file in a project. The list of
 * {@link AnalyzedTest}s contains a mix of static and dynamic data for each test that has been analyzed by Skippy.
 * <br /><br />
 * The JSON representation provides a good overview over the individual pieces:
 * <pre>
 * {
 *      "classes": {                                                    ──┐
 *          "0": {                                          ──┐           │
 *              "name": "com.example.Foo",                    │           │
 *              "path": "com/example/Foo.class",          ClassFile       │
 *              "outputFolder": "build/classes/java/main",    │           │
 *              "hash": "8E994DD8"                            │           │
 *          },                                              ──┘           │
 *          "1": {                                                        │
 *              "name": "com.example.FooTest",                            │
 *              "path": "com/example/FooTest.class",                      │
 *              "outputFolder": "build/classes/java/test",                │
 *              "hash": "5BC2F2A3"                                        │
 *          },                                                    ClassFileContainer
 *          "2": {                                                        │
 *              "name": "com.example.Bar",                                │
 *              "path": "com/example/Bar.class",                          │
 *              "outputFolder": "build/classes/java/main",                │
 *              "hash": "F7F27006"                                        │
 *          },                                                            │
 *          "3": {                                  ◄───────────────────────────────────┐
 *              "name": "com.example.BarTest",                            │             │
 *              "path": "com/example/BarTest.class",                      │             │
 *              "outputFolder": "build/classes/java/test",                │             │
 *              "hash": "7966371F"                                        │             │
 *          }                                                             │             │
 *      },                                                              ──┘    ids in AnalyzedTest
 *      "tests": [                                                              reference classes
 *          {                                               ──┐                in ClassFileContainer
 *              "class": 1,                                   │                         │
 *              "tags": ["PASSED"],                      AnalyzedTest                   │
 *              "coveredClasses": [0,1]                       │                         │
 *          },                                              ──┘                         │
 *          {                                                                           │
 *              "class": 3,   ──────────────┬───────────────────────────────────────────┘
 *              "tags": ["PASSED"],         │
 *              "coveredClasses": [2,3]  ───┘
 *          }
 *      ]
 * }
 * </pre>
 *
 * @author Florian McKee
 */
public final class TestImpactAnalysis {

    static final TestImpactAnalysis NOT_FOUND = new TestImpactAnalysis(ClassFileContainer.from(emptyList()), emptyList());
    private final ClassFileContainer classFileContainer;
    private final List<AnalyzedTest> analyzedTests;

    /**
     * Creates a new instance.
     *
     * @param classFileContainer a {@link ClassFileContainer}
     * @param analyzedTests a list of {@link AnalyzedTest}s
     */
    TestImpactAnalysis(ClassFileContainer classFileContainer, List<AnalyzedTest> analyzedTests) {
        this.classFileContainer = classFileContainer;
        this.analyzedTests = analyzedTests.stream().sorted().toList();
    }

    ClassFileContainer getClassFileContainer() {
        return classFileContainer;
    }

    List<AnalyzedTest> getAnalyzedTests() {
        return analyzedTests;
    }

    /**
     * Returns a unique identifier for this instance.
     *
     * @return a unique identifier for this instance
     */
    public String getId() {
        var builder = new StringBuilder();
        builder.append(classFileContainer.toJson());
        for (var analyzedTest : analyzedTests) {
            builder.append(analyzedTest.toJson());
        }
        return hashWith32Digits(builder.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Makes a skip-or-execute prediction for the test identified by the {@code testClassName}.
     *
     * @param testClassName the test's fully-qualified class name (e.g., com.example.FooTest)
     * @param configuration the {@link SkippyConfiguration}, must not be null
     * @param skippyRepository the {@link SkippyRepository}, must no tbe null
     * @return a skip-or-execute prediction for the test identified by the {@code testClassName}
     */
    PredictionWithReason predict(String testClassName, SkippyConfiguration configuration, SkippyRepository skippyRepository) {
        return Profiler.profile("TestImpactAnalysis#predict", () -> {
            if (NOT_FOUND.equals(this)) {
                return PredictionWithReason.execute(new Reason(TEST_IMPACT_ANALYSIS_NOT_FOUND, Optional.empty()));
            }
            var maybeAnalyzedTest = analyzedTests.stream()
                    .filter(test -> classFileContainer.getById(test.getTestClassId()).getClassName().equals(testClassName))
                    .findFirst();
            if (maybeAnalyzedTest.isEmpty()) {
                return PredictionWithReason.execute(new Reason(NO_DATA_FOUND_FOR_TEST, Optional.empty()));
            }
            var analyzedTest = maybeAnalyzedTest.get();
            var testClass = classFileContainer.getById(analyzedTest.getTestClassId());

            if (analyzedTest.isTaggedAs(TestTag.FAILED)) {
                return PredictionWithReason.execute(new Reason(TEST_FAILED_PREVIOUSLY, Optional.empty()));
            }

            if (testClass.classFileNotFound()) {
                return PredictionWithReason.execute(new Reason(TEST_CLASS_CLASS_FILE_NOT_FOUND, Optional.of(testClass.getPath().toString())));
            }

            if (testClass.hasChanged()) {
                return PredictionWithReason.execute(new Reason(BYTECODE_CHANGE_IN_TEST, Optional.empty()));
            }
            if (configuration.generateCoverageForSkippedTests()) {
                if (analyzedTest.getExecutionId().isEmpty()) {
                        return PredictionWithReason.execute(new Reason(MISSING_EXECUTION_ID, Optional.empty()));
                } else {
                    if (skippyRepository.readJacocoExecutionData(analyzedTest.getExecutionId().get()).isEmpty()) {
                        return PredictionWithReason.execute(new Reason(UNABLE_TO_READ_EXECUTION_DATA, Optional.empty()));
                    }
                }
            }
            for (var coveredClassId : analyzedTest.getCoveredClassesIds()) {
                var coveredClass = classFileContainer.getById(coveredClassId);
                if (coveredClass.classFileNotFound()) {
                    return PredictionWithReason.execute(new Reason(COVERED_CLASS_CLASS_FILE_NOT_FOUND, Optional.of(coveredClass.getPath().toString())));
                }
                if (coveredClass.hasChanged()) {
                    return PredictionWithReason.execute(new Reason(BYTECODE_CHANGE_IN_COVERED_CLASS, Optional.of(coveredClass.getClassName())));
                }
                var maybeCoveredTest = analyzedTests.stream()
                        .filter(test -> test.getTestClassId() == coveredClassId)
                        .findFirst();
                if (maybeCoveredTest.isPresent() && maybeCoveredTest.get().isTaggedAs(TestTag.FAILED)) {
                    var coveredTest = classFileContainer.getById(coveredClassId);
                    return PredictionWithReason.execute(new Reason(COVERED_TEST_FAILED_PREVIOUSLY, Optional.of(coveredTest.getClassName())));
                }
                if (maybeAnalyzedTest.isEmpty()) {
                    return PredictionWithReason.execute(new Reason(NO_DATA_FOUND_FOR_TEST, Optional.empty()));
                }
            }
            return PredictionWithReason.skip(new Reason(NO_CHANGE, Optional.empty()));
        });
    }

    /**
     * Returns the Jacoco execution ids from the {@link AnalyzedTest}s.
     *
     * @return the Jacoco execution ids from the {@link AnalyzedTest}s
     */
    List<String> getExecutionIds() {
        return analyzedTests.stream().flatMap(analyzedTest -> analyzedTest.getExecutionId().stream()).toList();
    }

    /**
     * Creates a {@link TestImpactAnalysis} from a JSON string.
     *
     * @param jsonString the JSON representation of a {@link TestImpactAnalysis}
     * @return the {@link TestImpactAnalysis} represented by the JSON string.
     */
    public static TestImpactAnalysis parse(String jsonString) {
        return Profiler.profile("TestImpactAnalysis#parse", () -> parse(new Tokenizer(jsonString)));
    }

    private static TestImpactAnalysis parse(Tokenizer tokenizer) {
        tokenizer.skip('{');
        ClassFileContainer classFileContainer = null;
        List<AnalyzedTest> analyzedTests = null;
        while (classFileContainer == null || analyzedTests == null) {
            var key = tokenizer.next();
            tokenizer.skip(':');
            switch (key) {
                case "id":
                    tokenizer.next();
                    break;
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
     * @return this instance as JSON string
     */
    public String toJson() {
        return """
            {
                "id": "%s",
                "classes": %s,
                "tests": [
            %s
                ]
            }""".formatted(
                getId(),
                classFileContainer.toJson(),
                analyzedTests.stream().map(c -> c.toJson()).collect(joining("," + lineSeparator())
            )
        );
    }

    /**
     * Merges two {@link TestImpactAnalysis} instances to support incremental updates of the data in the Skippy folder.
     *
     * @param other a {@link TestImpactAnalysis} instance that will be merged with this instance
     * @return a new instance that represents the merge of this and the {@code other} instance
     */
    TestImpactAnalysis merge(TestImpactAnalysis other) {
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
                remap(analyzedTest.getTestClassId(), original, merged),
                analyzedTest.getTags(),
                analyzedTest.getCoveredClassesIds().stream().map(id -> remap(id, original, merged)).toList(),
                analyzedTest.getExecutionId());
    }

    private int remap(int id, ClassFileContainer original, ClassFileContainer merged) {
        var classFile = original.getById(id);
        return merged.getId(classFile);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestImpactAnalysis that = (TestImpactAnalysis) o;
        return Objects.equals(classFileContainer, that.classFileContainer) && Objects.equals(analyzedTests, that.analyzedTests);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classFileContainer, analyzedTests);
    }
}