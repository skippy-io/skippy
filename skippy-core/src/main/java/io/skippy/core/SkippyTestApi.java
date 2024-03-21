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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.skippy.core.JacocoUtil.swallowJacocoExceptions;
import static io.skippy.core.SkippyConstants.PREDICTIONS_LOG_FILE;
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
    public static SkippyTestApi INSTANCE = getInstance();

    private final TestImpactAnalysis testImpactAnalysis;
    private final SkippyRepository skippyRepository;
    private final Map<String, Prediction> predictions = new ConcurrentHashMap<>();

    private SkippyTestApi(TestImpactAnalysis testImpactAnalysis, SkippyRepository skippyRepository) {
        this.testImpactAnalysis = testImpactAnalysis;
        this.skippyRepository = skippyRepository;
    }

    private static SkippyTestApi getInstance() {
        var skippyConfiguration = SkippyRepository.readConfiguration();
        var skippyRepository = SkippyRepository.getInstance(skippyConfiguration);
        var tia = skippyRepository.readTestImpactAnalysis().orElse(TestImpactAnalysis.NOT_FOUND);
        return new SkippyTestApi(tia, skippyRepository);
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
     * Prepares for the capturing of a JaCoCo execution data file for {@code testClass} before any tests in the class are executed.
     *
     * @param testClass a test class
     */
    public void prepareExecFileGeneration(Class<?> testClass) {
        Profiler.profile("SkippyTestApi#prepareCoverageDataCaptureFor", () -> {
            swallowJacocoExceptions(() -> {
                IAgent agent = RT.getAgent();
                agent.reset();
            });
        });
    }


    /**
     * Writes a JaCoCo execution data file after all tests in for {@code testClass} have been executed.
     *
     * @param testClass a test class
     */
    public void writeExecFile(Class<?> testClass) {
        Profiler.profile("SkippyTestApi#writeExecFile", () -> {
            swallowJacocoExceptions(() -> {
                IAgent agent = RT.getAgent();
                byte[] executionData = agent.getExecutionData(true);
                skippyRepository.saveTemporaryJaCoCoExecutionDataForCurrentBuild(testClass.getName(), executionData);
            });
        });
    }

}