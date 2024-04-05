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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import static java.nio.file.Files.*;

/**
 * Default {@link SkippyRepositoryExtension} implementation that
 * <ul>
 *     <li>stores and retrieves all data in / from the .skippy folder,</li>
 *     <li>only retains the latest {@link TestImpactAnalysis} and </li>
 *     <li>only retains the JaCoCo execution data files that are referenced by the latest {@link TestImpactAnalysis}.</li>
 * </ul>
 * It is intended for small projects that do not care about code coverage reports and thus do not need to store JaCoCo
 * execution data files. However, it supports projects of any size and the storage of JaCoCo execution data
 * files. This is useful for experimentation. It is not recommended to be used for large projects and / or
 * projects that want to store JaCoCo execution data files since it will significantly increase the size of your Git
 * repository.
 * <br /><br />
 * Large projects that want to store and more permanently retain {@link TestImpactAnalysis} instances and JaCoCo
 * execution data files should provide a custom implementation that stores those arteficts outside the project's
 * repository in storage systems
 * like
 * <ul>
 *     <li>databases,</li>
 *     <li>network file systems,</li>
 *     <li>blob storage like AWS S3,</li>
 *     <li>etc.</li>
 * </ul>
 *
 * @author Florian McKee
 */
final class DefaultRepositoryExtension implements SkippyRepositoryExtension {

    private final Path projectDir;

    public DefaultRepositoryExtension(Path projectDir) {
        this.projectDir = projectDir;
    }

    @Override
    public Optional<TestImpactAnalysis> findTestImpactAnalysis(String id) {
        try {
            var jsonFile = SkippyFolder.get(projectDir).resolve(Path.of("test-impact-analysis.json"));

            if (false == exists(jsonFile)) {
                return Optional.empty();
            }
            var tia = TestImpactAnalysis.parse(readString(jsonFile, StandardCharsets.UTF_8));
            if ( ! id.equals(tia.getId())) {
                return Optional.empty();
            }
            return Optional.of(tia);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read test impact analysis: %s.".formatted(e.getMessage()), e);
        }
    }

    @Override
    public void saveTestImpactAnalysis(TestImpactAnalysis testImpactAnalysis) {
        try {
            var jsonFile = SkippyFolder.get(projectDir).resolve(Path.of("test-impact-analysis.json"));
            Files.writeString(jsonFile, testImpactAnalysis.toJson(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            deleteTemporaryExecutionDataFilesForCurrentBuild();
            deleteObsoleteExecutionDataFiles(testImpactAnalysis);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to save test impact analysis %s: %s.".formatted(testImpactAnalysis.getId(), e.getMessage()), e);
        }
    }

    @Override
    public String saveJacocoExecutionData(byte[] jacocoExecutionData) {
        try {
            var executionId = JacocoUtil.getExecutionId(jacocoExecutionData);
            Files.write(SkippyFolder.get(projectDir).resolve("%s.exec".formatted(executionId)), zip(jacocoExecutionData), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return executionId;
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to save JaCoCo execution data: %s.".formatted(e.getMessage()), e);
        }
    }

    @Override
    public Optional<byte[]> findJacocoExecutionData(String testExecutionId) {
        try {
            var execFile = SkippyFolder.get(this.projectDir).resolve("%s.exec".formatted(testExecutionId));
            if (exists(execFile)) {
                return Optional.of(unzip(readAllBytes(execFile)));
            }
            return Optional.empty();
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read JaCoCo execution data %s: %s.".formatted(testExecutionId, e.getMessage()), e);
        }
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

    private static byte[] unzip(byte[] data) {
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

}