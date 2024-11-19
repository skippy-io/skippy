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

import org.jacoco.agent.rt.IAgent;
import org.jacoco.agent.rt.RT;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.skippy.core.JacocoUtil.mergeExecutionData;
import static io.skippy.core.JacocoUtil.swallowJacocoExceptions;
import static io.skippy.core.SkippyConstants.PREDICTIONS_LOG_FILE;
import static java.lang.System.lineSeparator;
import static java.nio.file.StandardOpenOption.*;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

/**
 * API that is used by Skippy's JUnit libraries to query for skip-or-execute predictions and to trigger the generation of .exec files.
 *
 * @author Florian McKee
 */
public final class SkippyTestApi {

    /**
     * The SkippyTestApi singleton.
     */
    public static SkippyTestApi INSTANCE = getInstance();

    private final TestImpactAnalysis testImpactAnalysis;
    private final SkippyRepository skippyRepository;
    private final PredictionModifier predictionModifier;
    private final SkippyConfiguration skippyConfiguration;
    private final Map<Class<?>, Prediction> predictions = new ConcurrentHashMap<>();

    /**
     * Stack that keeps track of the execution data across nested test classes.
     * <br /><br />
     * Example:
     * <pre>
     * {@literal @}PredictWithSkippy
     *  public class Level1 {
     *
     *     {@literal @}Nested
     *      class Level2 {
     *
     *         {@literal @}Nested
     *          class Level3 {
     *
     *             {@literal @}Test
     *              void testSomething() {
     *              }
     *
     *         }
     *
     *     }
     *
     * }
     * </pre>
     *
     *  By the time <code>testSomething</code> is executed, the stack would be populated as follows:
     *  <br /><br />
     *  <pre>
     *  frame 2 = { execution data for Level1$Level2$Level3.class }
     *  frame 1 = { execution data for Level1$Level2.class }
     *  frame 0 = { execution data for Level1.class }
     *  </pre>
     *
     *  The stack is used for two purposes:
     *  <ul>
     *      <li>It prevents the loss of execution data when the control flow changes from a parent to a nested test class.</li>
     *      <li>It allows nested tests classes to contribute their execution data back to the parents.</li>
     *  </ul>
     */
    private final Stack<List<byte[]>> executionDataStack = new Stack<>();

    SkippyTestApi(TestImpactAnalysis testImpactAnalysis, SkippyConfiguration skippyConfiguration, SkippyRepository skippyRepository) {
        this.testImpactAnalysis = testImpactAnalysis;
        this.skippyRepository = skippyRepository;
        this.predictionModifier = skippyConfiguration.predictionModifier();
        this.skippyConfiguration = skippyConfiguration;
    }

    private static SkippyTestApi getInstance() {
        var skippyConfiguration = SkippyRepository.readConfiguration();
        var skippyRepository = SkippyRepository.getInstance(skippyConfiguration);
        var tia = skippyRepository.readLatestTestImpactAnalysis();
        return new SkippyTestApi(tia, skippyConfiguration, skippyRepository);
    }

    /**
     * Tags a test.
     *
     * @param testClass the test's {@link Class}
     * @param tag the {@link TestTag}
     */
    public void tagTest(Class<?> testClass, TestTag tag) {
        skippyRepository.tagTest(testClass, tag);
    }

    /**
     * Returns {@code true} if {@code test} needs to be executed, {@code false} otherwise.
     *
     * @param test a class object representing a test
     * @return {@code true} if {@code test} needs to be executed, {@code false} otherwise
     */
    public boolean testNeedsToBeExecuted(Class<?> test) {
        return Profiler.profile("SkippyTestApi#testNeedsToBeExecuted", () -> {
            try {
                // re-use prediction made for the first test method in a class for all subsequent test methods
                if (predictions.containsKey(test)) {
                    return predictions.get(test) != Prediction.SKIP;
                }
                var predictionWithReason = predictionModifier.passThruOrModify(test, testImpactAnalysis.predict(test, skippyConfiguration, skippyRepository));

                // record {@link Prediction#ALWAYS_EXECUTE} as tags: this is required for JUnit 5's @Nested tests
                if (predictionWithReason.prediction() == Prediction.ALWAYS_EXECUTE) {
                    skippyRepository.tagTest(test, TestTag.ALWAYS_EXECUTE);
                }
                if (predictionWithReason.reason().details().isPresent()) {
                    Files.writeString(
                        SkippyFolder.get().resolve(PREDICTIONS_LOG_FILE),
                        "%s,%s,%s,\"%s\"%s".formatted(
                                test.getName(),
                                predictionWithReason.prediction(),
                                predictionWithReason.reason().category(),
                                predictionWithReason.reason().details().orElseGet(() -> "n/a"),
                                lineSeparator()),
                        StandardCharsets.UTF_8, CREATE, APPEND
                    );
                } else {
                    Files.writeString(
                            SkippyFolder.get().resolve(PREDICTIONS_LOG_FILE),
                            "%s,%s,%s%s".formatted(
                                    test.getName(),
                                    predictionWithReason.prediction(),
                                    predictionWithReason.reason().category(),
                                    lineSeparator()),
                            StandardCharsets.UTF_8, CREATE, APPEND
                    );
                }
                predictions.put(test, predictionWithReason.prediction());
                return predictionWithReason.prediction() != Prediction.SKIP;
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to check if test %s needs to be executed: %s.".formatted(test.getName(), e), e);
            }
        });
    }

    /**
     * Prepares for the capturing of a JaCoCo execution data file for {@code testClass} before any tests in the class are executed.
     *
     * @param testClass a test class
     */
    public void beforeTest(Class<?> testClass) {
        Profiler.profile("SkippyTestApi#prepareExecFileGeneration", () -> {
            swallowJacocoExceptions(() -> {
                IAgent agent = RT.getAgent();
                if (isNestedTest()) {
                    addExecutionDataToParent(asList(agent.getExecutionData(false)));
                }
                agent.reset();
                executionDataStack.push(new ArrayList<>());
            });
        });
    }

    /**
     * Writes a JaCoCo execution data and classpath file after all tests in for {@code testClass} have been executed:
     * <ul>
     *     <li>The execution data file contains the test's coverage</li>
     *     <li>The classpath file contains the test's classpath</li>
     * </ul>
     *
     * @param testClass a test class
     */
    public void afterTest(Class<?> testClass) {
        Profiler.profile("SkippyTestApi#writeExecAndClasspathFile", () -> {
            swallowJacocoExceptions(() -> {
                IAgent agent = RT.getAgent();
                var executionData = executionDataStack.lastElement();
                executionData.add(agent.getExecutionData(true));
                skippyRepository.afterTest(testClass, mergeExecutionData(executionData));
                executionDataStack.pop();
                if (isNestedTest()) {
                    addExecutionDataToParent(executionData);
                }
            });
        });
    }

    private boolean isNestedTest() {
        return ! executionDataStack.isEmpty();
    }

    private void addExecutionDataToParent(List<byte[]> executionData) {
        var parent = executionDataStack.lastElement();
        parent.addAll(executionData);
    }

}
