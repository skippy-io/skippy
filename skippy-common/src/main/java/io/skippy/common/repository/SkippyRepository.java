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

package io.skippy.common.repository;

import io.skippy.common.SkippyFolder;
import io.skippy.common.model.SkippyConfiguration;
import io.skippy.common.model.TestImpactAnalysis;
import io.skippy.common.model.TestWithJacocoExecutionDataAndCoveredClasses;
import io.skippy.common.util.JacocoExecutionDataUtil;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import static java.nio.file.Files.*;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * Repository for storage and retrieval of
 * <ul>
 *     <li>log and configuration files,</li>
 *     <li>temporary files that are used for communication between Skippy's JUnit and build libraries and</li>
 *     <li>{@link TestImpactAnalysis} instances and JaCoCo execution data.</li>
 * </ul>
 * Storage of JaCoCo execution data allows Skippy to generate test coverage reports that are equivalent to the ones
 * generated when running all tests.
 * <br /><br />
 * The default implementation
 * <ul>
 *     <li>stores and retrieves all data in / from the .skippy folder,</li>
 *     <li>only retains the latest {@link TestImpactAnalysis} and </li>
 *     <li>only retains the JaCoCo execution data files that are referenced by the latest {@link TestImpactAnalysis}.</li>
 * </ul>
 * It is intended for small projects that do not care about code coverage reports and thus not need to store JaCoCo
 * execution data files. However, it supports projects of any size and the storage of JaCoCo execution data
 * files. This is useful for experimentation. It is not recommended to be used for large projects and / or
 * projects that want to store JaCoCo execution data files since it will significantly increase the size of your Git
 * repository.
 * <br /><br />
 * Large projects that want to store and more permanently retain {@link TestImpactAnalysis} instances and JaCoCo
 * execution data files can replace
 * <ul>
 *     <li>{@link SkippyRepository#readTestImpactAnalysis()},</li>
 *     <li>{@link SkippyRepository#saveTestImpactAnalysis(TestImpactAnalysis)} and</li>
 *     <li>{@link SkippyRepository#readJacocoExecutionData(String)}</li>
 *     <li>{@link SkippyRepository#saveJacocoExecutionData}</li>
 * </ul>
 * with custom implementations to stores and retain those artefacts outside the project's repository in storage systems
 * like
 * <ul>
 *     <li>databases,</li>
 *     <li>network file systems,</li>
 *     <li>blob storage like AWS S3,</li>
 *     <li>etc.</li>
 * </ul>
 * Projects can implement and register a {@link SkippyRepositoryExtension} implementation to customize the aforementioned
 * methods.
 * <br /><br />
 * Example:
 * <pre>
 * package com.example;
 *
 * public class S3SkippyRepository implements SkippyRepositoryExtension {
 *
 *    {@literal @}Override
 *     public Optional&lt;TestImpactAnalysis&gt; readTestImpactAnalysis() {
 *         // read from S3
 *     }
 *
 *    {@literal @}Override
 *     public void saveTestImpactAnalysis(TestImpactAnalysis testImpactAnalysis) {
 *         // save in S3
 *     }
 *
 *    {@literal @}Override
 *     public Optional&lt;byte[]&gt; readJacocoExecutionData(String executionId) {
 *         // read from S3
 *     }
 *
 *    {@literal @}Override
 *     public String saveJacocoExecutionData(byte[] jacocoExecutionData) {
 *         // save in S3
 *     }
 *
 * }
 * </pre>
 * The custom implementation has to be registered with Skippy's build plugins.
 * <br /><br />
 * Gradle example:
 * <pre>
 * skippy {
 *     ...
 *     repository = 'com.example.S3SkippyRepository'
 * }
 * </pre>
 *
 * @author Florian McKee
 */
public final class SkippyRepository implements SkippyRepositoryExtension {

    private final Path projectDir;
    private final SkippyConfiguration skippyConfiguration;
    private final Optional<SkippyRepositoryExtension> extension = Optional.empty();

    private SkippyRepository(SkippyConfiguration skippyConfiguration, Path projectDir) {
        this.skippyConfiguration = skippyConfiguration;
        this.projectDir = projectDir;
    }

    /**
     * Returns the {@link SkippyRepository} instance for Skippy's build plugins.
     *
     * @param skippyConfiguration the {@link SkippyConfiguration}
     * @param projectDir the project directory
     * @return the {@link SkippyRepository}
     */
    public static SkippyRepository getInstance(SkippyConfiguration skippyConfiguration, Path projectDir) {
        return new SkippyRepository(skippyConfiguration, projectDir);
    }

    /**
     * Returns the {@link SkippyRepository} instance for Skippy's JUnit libraries.
     *
     * @param skippyConfiguration the {@link SkippyConfiguration}
     * @return the {@link SkippyRepository}
     */
    public static SkippyRepository getInstance(SkippyConfiguration skippyConfiguration) {
        return getInstance(skippyConfiguration, Path.of("."));
    }

    /**
     * Deletes the Skippy folder.
     */
    public void deleteSkippyFolder() {
        try {
            var skippyFolder = SkippyFolder.get(projectDir);
            if (exists(skippyFolder)) {
                try (var stream = newDirectoryStream(skippyFolder)) {
                    for (Path file : stream) {
                        delete(file);
                    }
                }
                delete(skippyFolder);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to delete the Skippy folder: %s.".formatted(e), e);
        }
    }

    /**
     * Reads the {@link SkippyConfiguration} from the Skippy folder.
     *
     * This method is intended for Skippy's JUnit libraries.
     *
     * @return  the {@link SkippyConfiguration}
     */
    public static SkippyConfiguration readConfiguration() {
        try {
            if (false == exists(SkippyFolder.get().resolve("config.json"))) {
                return SkippyConfiguration.DEFAULT;
            }
            return SkippyConfiguration.parse(Files.readString(SkippyFolder.get().resolve("config.json"), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read Skippy configuration: %s.".formatted(e.getMessage()), e);
        }
    }

    /**
     * Saves the {@link SkippyConfiguration} in the Skippy folder.
     *
     * @param skippyConfiguration the {@link SkippyConfiguration}
     */
    public void saveConfiguration(SkippyConfiguration skippyConfiguration) {
        try {
            Files.writeString(SkippyFolder.get(projectDir).resolve("config.json"), skippyConfiguration.toJson(), StandardCharsets.UTF_8, CREATE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to save Skippy configuration: %s.".formatted(e.getMessage()), e);
        }
    }

    /**
     * Deletes all log files from the Skippy folder.
     */
    public void deleteLogFiles() {
        try (var directoryStream  = Files.newDirectoryStream(SkippyFolder.get(projectDir),
                file -> file.getFileName().toString().endsWith(".log"))) {
            for (var logFile : directoryStream) {
                delete(logFile);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to delete log files: %s.".formatted(e.getMessage()), e);
        }
    }

    /**
     * Allows Skippy's JUnit libraries to temporarily save test execution data in the Skippy folder.
     * The data will be automatically deleted after the build finishes.
     *
     * @param testClassName the name of a test class (e.g., com.example.FooTest)
     * @param jacocoExecutionData Jacoco execution data for the test.
     */
    public void saveTemporaryJaCoCoExecutionDataForCurrentBuild(String testClassName, byte[] jacocoExecutionData) {
        try {
            Files.write(SkippyFolder.get(projectDir).resolve("%s.exec".formatted(testClassName)), jacocoExecutionData, CREATE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to save temporary test execution data file for current build: %s / %s.".formatted(testClassName, e.getMessage()), e);
        }
    }

    /**
     * Returns the test execution data written by {@link #saveTemporaryJaCoCoExecutionDataForCurrentBuild(String, byte[])}
     *
     * @return the test execution data written by {@link #saveTemporaryJaCoCoExecutionDataForCurrentBuild(String, byte[])}
     */
    public List<TestWithJacocoExecutionDataAndCoveredClasses> readTemporaryJaCoCoExecutionDataForCurrentBuild() {
        var result = new ArrayList<TestWithJacocoExecutionDataAndCoveredClasses>();
        for (var executionDataFile : getTemporaryExecutionDataFilesForCurrentBuild()) {
            var filename = executionDataFile.getFileName().toString();
            var testName = filename.substring(0, filename.indexOf(".exec"));
            try {
                var bytes = Files.readAllBytes(executionDataFile);
                result.add(new TestWithJacocoExecutionDataAndCoveredClasses(testName, bytes, JacocoExecutionDataUtil.getCoveredClasses(bytes)));
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to get temporary test execution data for current build: %s.".formatted(e.getMessage()), e);
            }
        }
        return result;
    }

    @Override
    public Optional<TestImpactAnalysis> readTestImpactAnalysis() {
        if (extension.isPresent()) {
            return extension.get().readTestImpactAnalysis();
        }
        try {
            var jsonFile = SkippyFolder.get(projectDir).resolve(Path.of("test-impact-analysis.json"));

            if (false == exists(jsonFile)) {
                return Optional.empty();
            }
            return Optional.of(TestImpactAnalysis.parse(Files.readString(jsonFile, StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read test impact analysis: %s.".formatted(e.getMessage()), e);
        }
    }

    @Override
    public Optional<byte[]> readJacocoExecutionData(String executionId) {
        try {
            var execFile = SkippyFolder.get(this.projectDir).resolve("%s.exec".formatted(executionId));
            if (exists(execFile)) {
                return Optional.of(unzip(readAllBytes(execFile)));
            }
            return Optional.empty();
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read JaCoCo execution data %s: %s.".formatted(executionId, e.getMessage()), e);
        }
    }

    @Override
    public void saveTestImpactAnalysis(TestImpactAnalysis testImpactAnalysis) {
        if (extension.isPresent()) {
            extension.get().saveTestImpactAnalysis(testImpactAnalysis);
            return;
        }
        try {
            var jsonFile = SkippyFolder.get(projectDir).resolve(Path.of("test-impact-analysis.json"));
            Files.writeString(jsonFile, testImpactAnalysis.toJson(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            deleteTemporaryExecutionDataFilesForCurrentBuild();
            deleteObsoleteExecutionDataFiles(testImpactAnalysis);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to save test impact analysis %s: %s.".formatted(testImpactAnalysis.getId(), e.getMessage()), e);
        }
    }


    /**
     * Saves Jacoco execution data for usage by subsequent builds.
     *
     * @param jacocoExecutionData Jacoco execution data
     * @return a unique identifier for the execution data (also referred to as execution id)
     */
    @Override
    public String saveJacocoExecutionData(byte[] jacocoExecutionData) {
        if (extension.isPresent()) {
            return extension.get().saveJacocoExecutionData(jacocoExecutionData);
        }
        try {
            var executionId = JacocoExecutionDataUtil.getExecutionId(jacocoExecutionData);
            Files.write(SkippyFolder.get(projectDir).resolve("%s.exec".formatted(executionId)), zip(jacocoExecutionData), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return executionId;
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to save JaCoCo execution data: %s.".formatted(e.getMessage()), e);
        }
    }

    private static byte[] zip(byte[] data) {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();

        byte[] buffer = new byte[1024];
        byte[] result = new byte[0];

        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            byte[] tmp = new byte[result.length + count];
            System.arraycopy(result, 0, tmp, 0, result.length);
            System.arraycopy(buffer, 0, tmp, result.length, count);
            result = tmp;
        }
        deflater.end();
        return result;
    }

    public static byte[] unzip(byte[] data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data);

        byte[] buffer = new byte[1024];
        byte[] result = new byte[0];

        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                byte[] tmp = new byte[result.length + count];
                System.arraycopy(result, 0, tmp, 0, result.length);
                System.arraycopy(buffer, 0, tmp, result.length, count);
                result = tmp;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            inflater.end();
        }

        return result;
    }

    private void deleteObsoleteExecutionDataFiles(TestImpactAnalysis testImpactAnalysis) {
        var executions = testImpactAnalysis.getExecutionIds();
        try (var directoryStream  = Files.newDirectoryStream(SkippyFolder.get(projectDir), path -> path.toString().endsWith(".exec"))) {
            for (var executionDataFile : directoryStream) {
                if (false == executions.contains(executionDataFile.getFileName().toString().replaceAll("\\.exec", ""))) {
                    delete(executionDataFile);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Deletion of obsolete execution data files failed: %s".formatted(e.getMessage()), e);
        }
    }


    private void deleteTemporaryExecutionDataFilesForCurrentBuild() {
        for (var executionDataFile : getTemporaryExecutionDataFilesForCurrentBuild()) {
            try {
                delete(executionDataFile);
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to delete %s.".formatted(executionDataFile), e);
            }
        }
    }

    private List<Path> getTemporaryExecutionDataFilesForCurrentBuild() {
        try {
            var result = new ArrayList<Path>();
            try (var directoryStream  = Files.newDirectoryStream(SkippyFolder.get(projectDir),
                    file -> file.getFileName().toString().endsWith(".exec") && false == file.getFileName().toString().matches("[A-Z0-9]{32}\\.exec"))) {
                for (var executionDataFile : directoryStream) {
                    result.add(executionDataFile);
                }
            }
            return result;
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to retrieve temporary execution data files for current build: %s".formatted(e), e);
        }
    }

}