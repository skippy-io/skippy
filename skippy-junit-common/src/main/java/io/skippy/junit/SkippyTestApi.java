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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jacoco.agent.rt.IAgent;
import org.jacoco.agent.rt.RT;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.SessionInfoStore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static io.skippy.core.SkippyConstants.SKIPPY_ANALYZE_MARKER;
import static java.nio.file.StandardOpenOption.*;
import java.util.LinkedList;

import static io.skippy.core.SkippyConstants.SKIPPY_DIRECTORY;

/**
 * API for generation of .cov files.
 *
 * @author Florian McKee
 */
public class SkippyTestApi {

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
