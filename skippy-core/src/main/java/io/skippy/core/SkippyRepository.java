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
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Stream;

import static io.skippy.core.ClassUtil.getOutputFolder;
import static java.nio.file.Files.*;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

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
        this.extension = skippyConfiguration.repositoryExtension(projectDir);
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
        return getInstance(skippyConfiguration, Path.of(""), null);
    }

    /**
     * Deletes the Skippy folder.
     */
    void resetSkippyFolder() {
        try {
            deleteTmpFolder();
            deleteLogFiles();
            deleteIfExists(SkippyFolder.get(projectDir).resolve("test-impact-analysis.json"));
            deleteIfExists(SkippyFolder.get(projectDir).resolve("LATEST"));
        } catch (IOException e) {
            throw new RuntimeException("Unable to reset skippy folder %s: %s".formatted(SkippyFolder.get(projectDir), e), e);
        }
    }

    void deleteTmpFolder() {
        deleteDirectory(SkippyFolder.get(projectDir).resolve("tmp"));
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
            throw new UncheckedIOException("Unable to read Skippy configuration: %s.".formatted(e), e);
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
            throw new UncheckedIOException("Unable to save Skippy configuration: %s.".formatted(e), e);
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
                        var className = line.split(",")[1];
                        var prediction = Prediction.valueOf(line.split(",")[2]);
                        return new ClassNameAndPrediction(className, prediction);
                    })
                    .toList();

        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read predictions log: %s.".formatted(e), e);
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
            throw new UncheckedIOException("Unable to delete log files: %s.".formatted(e), e);
        }
    }

    /**
     * Records the execution data for all tests in {@code testClass}.
     *
     * @param testClass the test {@link Class}
     * @param jacocoExecutionData Jacoco execution data for all tests in {@code testClass}
     */
    void afterAll(Class<?> testClass, byte[] jacocoExecutionData) {
        try {
            Files.write(
                getFolderWithTestRecording(testClass).resolve("%s.classpath".formatted(testClass.getName())),
                getClassPath(), CREATE, TRUNCATE_EXISTING
            );
            Files.write(getFolderWithTestRecording(testClass).resolve("%s.exec".formatted(testClass.getName())), jacocoExecutionData, CREATE, TRUNCATE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException("Unable to save temporary test execution data file for current build: %s / %s.".formatted(testClass.getName(), e), e);
        }
    }

    /**
     * Records the execution for {@code testMethod} in {@code testClass}.
     *
     * @param testClass the test {@link Class}
     * @param testMethod the name of the test method
     * @param jacocoExecutionData JaCoCo execution data for {@code testMethod}
     */
    void after(Class<?> testClass, String testMethod, byte[] jacocoExecutionData) {
        try {
            Files.write(
                getFolderWithTestRecording(testClass).resolve("%s.classpath".formatted(testClass.getName())),
                getClassPath(), CREATE, TRUNCATE_EXISTING
            );
            var execFile = getFolderWithTestRecording(testClass).resolve("%s.exec".formatted(testClass.getName()));
            if (exists(execFile)) {
                var merged = JacocoUtil.mergeExecutionData(asList(Files.readAllBytes(execFile), jacocoExecutionData));
                Files.write(getFolderWithTestRecording(testClass).resolve("%s.exec".formatted(testClass.getName())), merged, CREATE, TRUNCATE_EXISTING);
            } else {
                Files.write(getFolderWithTestRecording(testClass).resolve("%s.exec".formatted(testClass.getName())), jacocoExecutionData, CREATE, TRUNCATE_EXISTING);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to save temporary test execution data file for current build: %s / %s.".formatted(testClass.getName(), e), e);
        }
    }

    /**
     * Returns the test execution data written by {@link #afterAll(Class, byte[])}
     *
     * @return the test execution data written by {@link #afterAll(Class, byte[])}
     */
    List<TestRecording> getTestRecordings() {
        var result = new ArrayList<TestRecording>();
        var tmpDir = SkippyFolder.get(projectDir).resolve("tmp");
        if (false == exists(tmpDir)) {
            return emptyList();
        }
        try (Stream<Path> stream = Files.walk(tmpDir)) {
            stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".exec"))
                    .forEach(file -> {
                        var fileName = file.getFileName().toString();
                        var className = fileName.substring(0, fileName.lastIndexOf("."));
                        var outputFolder = tmpDir.relativize(file.getParent());
                        try {
                            var jacocoExecData = Files.readAllBytes(file);
                            var tagsFile = tmpDir.resolve(outputFolder).resolve("%s.tags".formatted(className));
                            var tags = new ArrayList<TestTag>();
                            if (exists(tagsFile)) {
                                tags.addAll(Files.readAllLines(tagsFile).stream().map(line -> TestTag.valueOf(line)).toList());
                            }
                            if (false == tags.contains(TestTag.FAILED)) {
                                tags.add(TestTag.PASSED);
                            }
                            result.add(new TestRecording(className, outputFolder, tags, JacocoUtil.getCoveredClasses(jacocoExecData), jacocoExecData));
                        } catch (IOException e) {
                            throw new RuntimeException("Unable to read recorded test data for current build: %s.".formatted(e), e);
                        }
                    });
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Unable to read recorded test data for current build: %s.".formatted(e), e);
        }
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
            throw new UncheckedIOException("Unable to save execution data for skipped tests: %s.".formatted(e), e);
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
            throw new UncheckedIOException("Unable to read latest test impact analysis: %s.".formatted(e), e);
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
            deleteTmpFolder();
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

    /**
     * Tags a test.
     *
     * @param testClass the test
     * @param tag the {@link TestTag}
     */
    public void tagTest(Class<?> testClass, TestTag tag) {
        var tagsFile = getFolderWithTestRecording(testClass).resolve("%s.tags".formatted(testClass.getName()));
        try {
            Files.write(tagsFile, asList(tag.name()), StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to tag test %s in file %s: %s.".formatted(testClass.getName(), tagsFile, e), e);
        }
    }

    /**
     * The classpath entries that point to the project folder (as folders relative to the project folder):
     * <br /><br />
     * Example:
     * <pre>
     * build/classes/java/test
     * build/classes/java/main
     * build/resources/main
     * </pre>
     *
     * @return classpath entries that point to the project folder
     */
    private List<String> getClassPath() {
        return asList(System.getProperty("java.class.path")
                .split(System.getProperty("path.separator")))
                .stream()
                .filter(entry -> entry.startsWith(projectDir.toAbsolutePath().toString()))
                .map(entry -> entry.substring(projectDir.toAbsolutePath().toString().length() + 1))
                .toList();
    }

    private Path getFolderWithTestRecording(Class<?> testClass) {
        try {
            var execFileFolder = SkippyFolder.get(projectDir)
                    .resolve("tmp")
                    .resolve(getOutputFolder(projectDir, testClass));
            return Files.createDirectories(execFileFolder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteDirectory(Path path) {
        if (Files.isDirectory(path)) {
            try {
                Files.walkFileTree(path, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException("Unable to delete directory %s: %s.".formatted(path, e), e);
            }
        }
    }

    void log(String statement) {
        var logFile = SkippyFolder.get(projectDir).resolve("logging.log");
        try {
            Files.write(logFile, asList(statement), StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException("Unable to write log statement: %s.".formatted(e), e);
        }
    }
}

