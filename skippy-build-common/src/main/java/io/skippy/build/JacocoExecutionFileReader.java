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

package io.skippy.build;

import io.skippy.common.SkippyFolder;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.SessionInfoStore;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.ServerError;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Reads Jacoco execution data files that have been created by Skippy's JUnit libraries.
 *
 * The main purpose of this abstraction is to simplify unit testing of {@link SkippyBuildApi}.
 *
 * @author Florian McKee
 */
final class JacocoExecutionFileReader {

    /**
     * Returns all Jacoco execution data files in the Skippy folder.
     *
     * @param projectDir the project folder
     * @return all Jacoco execution data files in the Skippy folder
     */
    List<Path> getJacocoExecutionDataFiles(Path projectDir) {
        File[] files = SkippyFolder.get(projectDir).toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".exec"));
        return asList(files).stream()
                .filter(file -> ! file.getName().matches("[A-Z0-9]{32}\\.exec"))
                .map(File::toPath).toList();
    }

    /**
     * Extracts the class names of all covered classes from an execution data file.
     *
     * @param jacocoExecutionDataFile a Jacoco execution data file
     * @return the class names of all covered classes from an execution data file
     */
    List<String> getCoveredClasses(Path jacocoExecutionDataFile) {
        try {
            var coveredClasses = new LinkedList<String>();
            ExecutionDataReader executionDataReader = new ExecutionDataReader(new ByteArrayInputStream(getJacocoExecutionData(jacocoExecutionDataFile)));
            executionDataReader.setSessionInfoVisitor(new SessionInfoStore());
            executionDataReader.setExecutionDataVisitor(visitor -> coveredClasses.add(visitor.getName().replace("/", ".").trim()));
            executionDataReader.read();
            return coveredClasses;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the binary content of a Jacoco execution data file.
     *
     * @param jacocoExecutionDataFile a Jacoco execution data file
     * @return the binary content of a Jacoco execution data file
     */
    byte[] getJacocoExecutionData(Path jacocoExecutionDataFile) {
        try {
            return Files.readAllBytes(jacocoExecutionDataFile);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
