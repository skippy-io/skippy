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

import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.data.SessionInfoStore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * JaCoCo related utility methods.
 *
 * @author Florian McKee
 */
class JacocoUtil {

    private static final Logger LOGGER = Logger.getLogger(JacocoUtil.class.getName());

    /**
     * Executes the {@code runnable} and swallows certain JaCoCo related exceptions to prevent build failures
     * when the JaCoCo agent is not running.
     *
     * @param runnable a {@link Runnable}
     */
    static void swallowJacocoExceptions(Runnable runnable) {
        try {
            runnable.run();
        } catch (NoClassDefFoundError e) {
            if (e.getMessage().startsWith("org/jacoco")) {
                LOGGER.severe("Unable to load JaCoCo class %s".formatted(e.getMessage()));
                LOGGER.severe("");
                LOGGER.severe("Did you forget to add the Jacoco plugin to your build file?");

                // suppress exception to continue the build

            } else {
                throw e;
            }
        } catch (IllegalStateException e) {
            if (e.getMessage().equals("JaCoCo agent not started.")) {
                LOGGER.severe("Jacoco agent unavailable: %s".formatted(e.getMessage()));
                LOGGER.severe("");
                LOGGER.severe("Did you forget to add the Jacoco plugin to your build file?");

                // suppress exception to continue the build

            } else {
                throw e;
            }
        }
    }

    /**
     * Extracts the names of the classes from JaCoCo execution data.
     *
     * @param jacocoExecutionData JaCoCo execution data
     * @return the names of the classes that are covered by the JaCoCo execution data
     */
    static List<String> getCoveredClasses(byte[] jacocoExecutionData) {
        try {
            var coveredClasses = new LinkedList<String>();
            var reader = new ExecutionDataReader(new ByteArrayInputStream(jacocoExecutionData));
            reader.setSessionInfoVisitor(new SessionInfoStore());
            reader.setExecutionDataVisitor(visitor -> coveredClasses.add(visitor.getName().replace("/", ".").trim()));
            reader.read();
            return coveredClasses.stream().sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to compute covered classes for JaCoCo execution data: %s.".formatted(e.getMessage()), e);
        }
    }

    /**
     * Generates an identifier that uniquely identifies the execution data (ignoring the session info data).
     * If two execution data arrays are equivalent except the data in the session info block, this method will
     * generate the same ids for both.
     *
     * @param jacocoExecutionData Jacoco execution data
     * @return an identifier that uniquely identifies the execution data (ignoring the session info data)
     */
    static String getExecutionId(byte[] jacocoExecutionData)  {
        try {
            var byteArrayOutputStream = new ByteArrayOutputStream();
            var writer = new ExecutionDataWriter(byteArrayOutputStream);
            var reader = new ExecutionDataReader(new ByteArrayInputStream(jacocoExecutionData));
            reader.setSessionInfoVisitor(new SessionInfoStore());
            reader.setExecutionDataVisitor(executionData -> writer.visitClassExecution(executionData));
            reader.read();
            return HashUtil.hashWith32Digits(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to compute execution id from JaCoCo execution data: %s.".formatted(e.getMessage()), e);
        }
    }
}
