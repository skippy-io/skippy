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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static java.nio.file.Files.*;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.*;

/**
 * Repository for storage and retrieval of
 * <ul>
 *     <li>log and configuration files,</li>
 *     <li>temporary files that are used for communication between Skippy's JUnit and build libraries and</li>
 *     <li>{@link TestImpactAnalysis} instances and JaCoCo execution data.</li>
 * </ul>
 * Storage of JaCoCo execution data allows Skippy to generate test coverage reports that are equivalent to the ones
 * generated when running all tests.
 *
 * @author Florian McKee
 */
public final class SkippyRepository {

    private final Path projectDir;
    private final Path buildDir;
    private final SkippyRepositoryExtension extension;

    private SkippyRepository(SkippyConfiguration skippyConfiguration, Path projectDir, Path buildDir) {
        this.projectDir = projectDir;
        this.buildDir = buildDir;
        this.extension = createRepositoryExtension(skippyConfiguration, projectDir);
    }

    /**
     * Returns the {@link SkippyRepository} instance for Skippy's build plugins.
     *
     * @param skippyConfiguration the {@link SkippyConfiguration}
     * @param projectDir the project directory (e.g., ~/repo)
     * @param buildDirectory the project's build directory (e.g., ~/repo/build or ~/repo/target)
     * @return the {@link SkippyRepository}
     */
    public static SkippyRepository getInstance(SkippyConfiguration skippyConfiguration, Path projectDir, Path buildDirectory) {
        return new SkippyRepository(skippyConfiguration, projectDir, buildDirectory);
    }

    /**
     * Returns the {@link SkippyRepository} instance for Skippy's JUnit libraries.
     *
     * @param skippyConfiguration the {@link SkippyConfiguration}
     * @return the {@link SkippyRepository}
     */
    public static SkippyRepository getInstance(SkippyConfiguration skippyConfiguration) {
        return getInstance(skippyConfiguration, Path.of("."), null);
    }

    /**
     * Deletes the Skippy folder.
     */
    void deleteSkippyFolder() {
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
    static SkippyConfiguration readConfiguration() {
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
    void saveConfiguration(SkippyConfiguration skippyConfiguration) {
        try {
            Files.writeString(SkippyFolder.get(projectDir).resolve("config.json"), skippyConfiguration.toJson(), StandardCharsets.UTF_8, CREATE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to save Skippy configuration: %s.".formatted(e.getMessage()), e);
        }
    }

    /**
     * Reads the predictions.log file in the Skippy folder. The return type is a list of {@link ClassNameAndPrediction}s with the left
     * element being the class name of a test, and the right element being the {@link Prediction} for that test.
     *
     * @return the contents of the predictions.log file in the Skippy folder
     */
    List<ClassNameAndPrediction> readPredictionsLog() {
        try {
            var predictionsLog = SkippyFolder.get(projectDir).resolve("predictions.log");
            if (false == exists(predictionsLog)) {
                return emptyList();
            }
            return Files.readAllLines(predictionsLog, StandardCharsets.UTF_8).stream().
                    map(line -> {
                        var className = line.split(",")[0];
                        var prediction = Prediction.valueOf(line.split(",")[1]);
                        return new ClassNameAndPrediction(className, prediction);
                    })
                    .toList();

        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read predictions log: %s.".formatted(e.getMessage()), e);
        }
    }

    /**
     * Deletes all log files from the Skippy folder.
     */
    void deleteLogFiles() {
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
    void saveTemporaryJaCoCoExecutionDataForCurrentBuild(String testClassName, byte[] jacocoExecutionData) {
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
    List<TestWithJacocoExecutionDataAndCoveredClasses> readTemporaryJaCoCoExecutionDataForCurrentBuild() {
        var result = new ArrayList<TestWithJacocoExecutionDataAndCoveredClasses>();
        for (var executionDataFile : getTemporaryExecutionDataFilesForCurrentBuild()) {
            var filename = executionDataFile.getFileName().toString();
            var testName = filename.substring(0, filename.indexOf(".exec"));
            try {
                var bytes = Files.readAllBytes(executionDataFile);
                result.add(new TestWithJacocoExecutionDataAndCoveredClasses(testName, bytes, JacocoUtil.getCoveredClasses(bytes)));
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to get temporary test execution data for current build: %s.".formatted(e.getMessage()), e);
            }
        }
        return result;
    }

    /**
     * Saves the execution data for skipped tests as file named skipped.exec in the build directory.
     *
     * @param executionDataForSkippedTests Jacoco execution data for skipped tests
     */
    void saveExecutionDataForSkippedTests(byte[] executionDataForSkippedTests) {
        try {
            Files.write(buildDir.resolve("skippy.exec"), executionDataForSkippedTests, CREATE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to save execution data for skipped tests: %s.".formatted(e.getMessage()), e);
        }
    }

    TestImpactAnalysis readLatestTestImpactAnalysis() {
        try {
            var versionFile = SkippyFolder.get(projectDir).resolve(Path.of("LATEST"));
            if (exists(versionFile)) {
                var id = Files.readString(versionFile, StandardCharsets.UTF_8);
                return extension.findTestImpactAnalysis(id).orElse(TestImpactAnalysis.NOT_FOUND);
            }
            return TestImpactAnalysis.NOT_FOUND;
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read latest test impact analysis: %s.".formatted(e.getMessage()), e);
        }
    }

    private SkippyRepositoryExtension createRepositoryExtension(SkippyConfiguration skippyConfiguration, Path projectDir) {
        try {
            Class<?> clazz = Class.forName(skippyConfiguration.repositoryClass());
            Constructor<?> constructor = clazz.getConstructor(Path.class);
            return (SkippyRepositoryExtension) constructor.newInstance(projectDir);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create repository extension %s: %s.".formatted(skippyConfiguration.repositoryClass(), e.getMessage()), e);
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


    void saveTestImpactAnalysis(TestImpactAnalysis testImpactAnalysis) {
        try {
            var versionFile = SkippyFolder.get(projectDir).resolve(Path.of("LATEST"));
            Files.writeString(versionFile, testImpactAnalysis.getId(), StandardCharsets.UTF_8, CREATE, TRUNCATE_EXISTING);
            extension.saveTestImpactAnalysis(testImpactAnalysis);
            deleteTemporaryExecutionDataFilesForCurrentBuild();
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to save TestImpactAnalysis %s: %s".formatted(testImpactAnalysis.getId(), e), e);
        }
    }

    Optional<byte[]> readJacocoExecutionData(String executionId) {
        return extension.findJacocoExecutionData(executionId);
    }

    String saveJacocoExecutionData(byte[] jacocoExecutionData) {
        var executionId = JacocoUtil.getExecutionId(jacocoExecutionData);
        extension.saveJacocoExecutionData(executionId, jacocoExecutionData);
        return executionId;
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

    void deleteTestTags() {
        var tagsFile = SkippyFolder.get(projectDir).resolve("tags.txt");
        if (exists(tagsFile)) {
            try {
                delete(tagsFile);
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to delete %s.".formatted(tagsFile), e);
            }
        }
    }

    void tagTest(String className, TestTag tag) {
        var tagsFile = SkippyFolder.get(projectDir).resolve("tags.txt");
        try {
            Files.write(tagsFile, asList("%s=%s".formatted(className, tag.name())), StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to tag test %s in file %s: %s.".formatted(className, tagsFile, e.getMessage()), e);
        }
    }

    List<TestTag> getTestTags(String className) {
        return getTestTags().getOrDefault(className, asList(TestTag.PASSED));
    }

    private Map<String, List<TestTag>> getTestTags() {
        var tagsFile = SkippyFolder.get(projectDir).resolve("tags.txt");
        try {
            if (false == exists(tagsFile)) {
                return emptyMap();
            }
            return Files.lines(tagsFile)
                .map(line -> line.split("=", 2))
                .collect(groupingBy(parts -> parts[0], mapping(parts -> TestTag.valueOf(parts[1]), toList())));
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read tags file %s: %s.".formatted(tagsFile, e.getMessage()), e);
        }
    }

}

