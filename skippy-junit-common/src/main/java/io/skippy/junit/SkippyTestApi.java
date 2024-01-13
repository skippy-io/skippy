/*
 * Copyright 2023 the original author or authors.
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

import io.skippy.core.Profiler;
import org.jacoco.agent.rt.IAgent;
import org.jacoco.agent.rt.RT;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.SessionInfoStore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static io.skippy.core.SkippyConstants.*;
import static io.skippy.junit.JaCoCoExceptionHandler.swallowJaCoCoExceptions;
import static java.nio.file.StandardOpenOption.*;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API to query for skip-or-execute predictions and to trigger the generation of .cov files.
 *
 * @author Florian McKee
 */
public final class SkippyTestApi {

    private static final boolean WRITE_EXEC_FILE = false;

    /**
     * The SkippyTestApi singleton.
     */
    public static final SkippyTestApi INSTANCE = new SkippyTestApi(SkippyAnalysis.INSTANCE);

    private final SkippyAnalysis skippyAnalysis;
    private final Map<Class<?>, SkippyAnalysis.Prediction> predictions = new ConcurrentHashMap<>();

    private SkippyTestApi(SkippyAnalysis skippyAnalysis) {
        this.skippyAnalysis = skippyAnalysis;
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
                if (predictions.containsKey(test)) {
                    return predictions.get(test) == SkippyAnalysis.Prediction.EXECUTE;
                }
                var predictionWithReason = skippyAnalysis.predict(new FullyQualifiedClassName(test.getName()));

                if ( ! SKIPPY_DIRECTORY.toFile().exists()) {
                    SKIPPY_DIRECTORY.toFile().mkdirs();
                }

                Files.writeString(
                    SKIPPY_DIRECTORY.resolve(PREDICTIONS_LOG_FILE),
                    "%s:%s:%s%s".formatted(test.getName(), predictionWithReason.prediction(), predictionWithReason.reason(), System.lineSeparator()),
                    StandardCharsets.UTF_8, CREATE, APPEND
                );
                predictions.put(test, predictionWithReason.prediction());
                return predictionWithReason.prediction() == SkippyAnalysis.Prediction.EXECUTE;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    /**
     * Prepares for the capturing of a .cov file for {@code testClass} before any tests in the class are executed.
     *
     * @param testClass a test class
     */
    public static void prepareCoverageDataCaptureFor(Class<?> testClass) {
        Profiler.profile("SkippyTestApi#prepareCoverageDataCaptureFor", () -> {
            if ( ! testImpactAnalysisIsRunning()) {
                return;
            }
            swallowJaCoCoExceptions(() -> {
                IAgent agent = RT.getAgent();
                agent.reset();
            });
        });
    }


    /**
     * Captures a .cov file for {@code testClass} after all tests in the class have been executed.
     *
     * @param testClass a test class
     */
    public static void captureCoverageDataFor(Class<?> testClass) {
        Profiler.profile("SkippyTestApi#captureCoverageDataFor", () -> {
            // this property / environment variable is set by Skippy's build plugins whenever a build performs a Skippy analysis
            if ( ! testImpactAnalysisIsRunning()) {
                return;
            }
            swallowJaCoCoExceptions(() -> {
                writeCovFileFor(testClass);
            });
        });
    }

    private static void writeCovFileFor(Class<?> testClass) {
        IAgent agent = RT.getAgent();
        var coveredClasses = new LinkedList<String>();
        byte[] executionData = agent.getExecutionData(true);
        ExecutionDataReader executionDataReader = new ExecutionDataReader(new ByteArrayInputStream(executionData));
        executionDataReader.setSessionInfoVisitor(new SessionInfoStore());
        executionDataReader.setExecutionDataVisitor(visitor -> coveredClasses.add(visitor.getName()));
        try {
            executionDataReader.read();
            var name = testClass.getName();

            if ( ! SKIPPY_DIRECTORY.toFile().exists()) {
                SKIPPY_DIRECTORY.toFile().mkdirs();
            }

            Files.write(SKIPPY_DIRECTORY.resolve("%s.cov".formatted(name)), coveredClasses, StandardCharsets.UTF_8,
                    CREATE, TRUNCATE_EXISTING);
            if (WRITE_EXEC_FILE) {
                Files.write(SKIPPY_DIRECTORY.resolve("%s.exec".formatted(name)), executionData,
                        CREATE, TRUNCATE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write execution data: %s".formatted(e.getMessage()), e);
        }
    }

    private static boolean testImpactAnalysisIsRunning() {
        // the property / environment variable is set by Skippy's build plugins whenever a build performs a Skippy analysis
        return Boolean.valueOf(System.getProperty(TEST_IMPACT_ANALYSIS_RUNNING)) ||
                Boolean.valueOf(System.getenv().get(TEST_IMPACT_ANALYSIS_RUNNING));
    }

}
