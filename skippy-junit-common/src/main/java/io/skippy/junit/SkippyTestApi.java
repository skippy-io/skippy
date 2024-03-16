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

package io.skippy.junit;

import io.skippy.common.SkippyFolder;
import io.skippy.common.model.Prediction;
import io.skippy.common.model.TestImpactAnalysis;
import io.skippy.common.repository.SkippyRepository;
import io.skippy.common.util.Profiler;
import org.jacoco.agent.rt.IAgent;
import org.jacoco.agent.rt.RT;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.skippy.common.SkippyConstants.PREDICTIONS_LOG_FILE;
import static io.skippy.junit.JacocoExceptionHandler.swallowJacocoExceptions;
import static java.lang.System.lineSeparator;
import static java.nio.file.StandardOpenOption.*;

/**
 * API that is used by Skippy's JUnit libraries to query for skip-or-execute predictions and to trigger the generation of .exec files.
 *
 * @author Florian McKee
 */
public final class SkippyTestApi {

    /**
     * The SkippyTestApi singleton.
     */
    public static final SkippyTestApi INSTANCE = new SkippyTestApi(
            SkippyRepository.getInstance().readTestImpactAnalysis().orElse(TestImpactAnalysis.NOT_FOUND));

    private final TestImpactAnalysis testImpactAnalysis;
    private final Map<String, Prediction> predictions = new ConcurrentHashMap<>();

    private SkippyTestApi(TestImpactAnalysis testImpactAnalysis) {
        this.testImpactAnalysis = testImpactAnalysis;
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
                if (predictions.containsKey(test.getName())) {
                    return predictions.get(test.getName()) == Prediction.EXECUTE;
                }
                var predictionWithReason = testImpactAnalysis.predict(test.getName());
                if (predictionWithReason.reason().details().isPresent()) {
                    Files.writeString(
                        SkippyFolder.get().resolve(PREDICTIONS_LOG_FILE),
                        "%s,%s,%s,%s%s".formatted(
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
                predictions.put(test.getName(), predictionWithReason.prediction());
                return predictionWithReason.prediction() == Prediction.EXECUTE;
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to check if test %s needs to be executed: %s.".formatted(test.getName(), e.getMessage()), e);
            }
        });
    }

    /**
     * Prepares for the capturing of a .exec file for {@code testClass} before any tests in the class are executed.
     *
     * @param testClass a test class
     */
    public static void prepareExecFileGeneration(Class<?> testClass) {
        Profiler.profile("SkippyTestApi#prepareCoverageDataCaptureFor", () -> {
            swallowJacocoExceptions(() -> {
                IAgent agent = RT.getAgent();
                agent.reset();
            });
        });
    }


    /**
     * Writes a .exec file to the Skippy folder after all tests in for {@code testClass} have been executed.
     *
     * @param testClass a test class
     */
    public static void writeExecFile(Class<?> testClass) {
        Profiler.profile("SkippyTestApi#writeExecFile", () -> {
            swallowJacocoExceptions(() -> {
                IAgent agent = RT.getAgent();
                byte[] executionData = agent.getExecutionData(true);
                SkippyRepository.getInstance().saveTemporaryJaCoCoExecutionDataForCurrentBuild(testClass.getName(), executionData);
            });
        });
    }

}