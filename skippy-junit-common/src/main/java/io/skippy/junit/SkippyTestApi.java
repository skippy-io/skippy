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
import static java.nio.file.StandardOpenOption.*;

import java.util.LinkedList;

/**
 * API to query for skip-or-execute decisions and to trigger the generation of .cov files.
 *
 * @author Florian McKee
 */
public final class SkippyTestApi {

    /**
     * The SkippyTestApi singleton.
     */
    public static final SkippyTestApi INSTANCE = new SkippyTestApi(SkippyAnalysis.INSTANCE);

    private final SkippyAnalysis skippyAnalysis;

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
        try {
            var decision = skippyAnalysis.decide(new FullyQualifiedClassName(test.getName()));
            Files.writeString(
                    SKIPPY_DIRECTORY.resolve(SKIP_OR_EXECUTE_DECISION_FILE),
                    "%s:%s:%s%s".formatted(test.getName(), decision.decision(), decision.reason(), System.lineSeparator()),
                    StandardCharsets.UTF_8, CREATE, APPEND
            );
            return decision.decision() == SkippyAnalysis.Decision.EXECUTE_TEST;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Prepares for the capturing of a .cov file for {@code testClass} before any tests in the class are executed.
     *
     * @param testClass a test class
     */
    public static void prepareCoverageDataCaptureFor(Class<?> testClass) {
        // this property / environment variable is set by Skippy's build plugins whenever a build performs a Skippy analysis
        if ( ! isSkippyCoverageBuild()) {
            return;
        }
        IAgent agent = RT.getAgent();
        agent.reset();
    }


    /**
     * Captures a .cov file for {@code testClass} after all tests in the class have been executed.
     *
     * @param testClass a test class
     */
    public static void captureCoverageDataFor(Class<?> testClass) {
        // this property / environment variable is set by Skippy's build plugins whenever a build performs a Skippy analysis
        if ( ! isSkippyCoverageBuild()) {
            return;
        }
        IAgent agent = RT.getAgent();
        var coveredClasses = new LinkedList<String>();
        byte[] executionData = agent.getExecutionData(true);
        ExecutionDataReader executionDataReader = new ExecutionDataReader(new ByteArrayInputStream(executionData));
        executionDataReader.setSessionInfoVisitor(new SessionInfoStore());
        executionDataReader.setExecutionDataVisitor(visitor -> coveredClasses.add(visitor.getName()));
        try {
            executionDataReader.read();
            var name = testClass.getName();
            Files.write(SKIPPY_DIRECTORY.resolve("%s.cov".formatted(name)), coveredClasses, StandardCharsets.UTF_8,
                    CREATE, TRUNCATE_EXISTING);
            Files.write(SKIPPY_DIRECTORY.resolve("%s.exec".formatted(name)), executionData,
                    CREATE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write execution data: %s".formatted(e.getMessage()), e);
        }
    }

    private static boolean isSkippyCoverageBuild() {
        return Boolean.valueOf(System.getProperty(SKIPPY_ANALYZE_MARKER)) || Boolean.valueOf(System.getenv().get(SKIPPY_ANALYZE_MARKER));
    }

}
