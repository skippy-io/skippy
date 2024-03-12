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

package io.skippy.common.util;

import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.data.SessionInfoStore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedList;
import java.util.List;

import static io.skippy.common.util.HashUtil.hashWith32Digits;

/**
 * Utility methods that operate on JaCoCo execution data.
 *
 * @author Florian McKee
 */
public class JacocoExecutionDataUtil {

    /**
     * Extracts the names of the classes that are covered by the JaCoCo execution data that was captured for a test.
     *
     * @param jacocoTestExecutionData JaCoCo execution data that was captured for a test
     * @return the names of the classes that are covered by the JaCoCo execution data that was captured for a test
     */
    public static List<String> getCoveredClasses(byte[] jacocoTestExecutionData) {
        try {
            var coveredClasses = new LinkedList<String>();
            var reader = new ExecutionDataReader(new ByteArrayInputStream(jacocoTestExecutionData));
            reader.setSessionInfoVisitor(new SessionInfoStore());
            reader.setExecutionDataVisitor(visitor -> coveredClasses.add(visitor.getName().replace("/", ".").trim()));
            reader.read();
            return coveredClasses;
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
    public static String getExecutionId(byte[] jacocoExecutionData)  {
        try {
            var byteArrayOutputStream = new ByteArrayOutputStream();
            var writer = new ExecutionDataWriter(byteArrayOutputStream);
            var reader = new ExecutionDataReader(new ByteArrayInputStream(jacocoExecutionData));
            reader.setSessionInfoVisitor(new SessionInfoStore());
            reader.setExecutionDataVisitor(executionData -> writer.visitClassExecution(executionData));
            reader.read();
            return hashWith32Digits(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to compute execution id from JaCoCo execution data: %s.".formatted(e.getMessage()), e);
        }
    }
}
