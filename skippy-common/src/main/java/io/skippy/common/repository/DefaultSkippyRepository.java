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
import io.skippy.common.model.TestWithJacocoExecutionDataAndCoveredClasses;
import io.skippy.common.model.TestImpactAnalysis;
import io.skippy.common.util.JacocoExecutionDataUtil;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.zip.Deflater;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.Arrays.asList;

/**
 * {@link SkippyRepository} that stores and retrieves all data in / from the .skippy folder.
 */
class DefaultSkippyRepository implements SkippyRepository {

    private final Path projectDir;

    DefaultSkippyRepository(Path projectDir) {
        this.projectDir = projectDir;
    }

    @Override
    public List<TestWithJacocoExecutionDataAndCoveredClasses> getTemporaryTestExecutionDataForCurrentBuild() {
        var result = new ArrayList<TestWithJacocoExecutionDataAndCoveredClasses>();
        List<Path> executionDataFiles = getTemporaryExecutionDataFilesForCurrentBuild();
        for (var executionDataFile : executionDataFiles) {
        var testName = executionDataFile.toFile().getName().substring(0, executionDataFile.toFile().getName().indexOf(".exec"));
            try {
                var bytes = Files.readAllBytes(executionDataFile);
                result.add(new TestWithJacocoExecutionDataAndCoveredClasses(testName, bytes, JacocoExecutionDataUtil.getCoveredClasses(bytes)));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return result;
    }

    @Override
    public TestImpactAnalysis readTestImpactAnalysis() {
        var idFile = SkippyFolder.get(projectDir).resolve(Path.of("LATEST"));
        if (false == idFile.toFile().exists()) {
            return TestImpactAnalysis.NOT_FOUND;
        }
        try {
            var id = Files.readString(idFile, StandardCharsets.UTF_8);
            var jsonFile = SkippyFolder.get(projectDir).resolve(Path.of("tia_%s.json".formatted(id)));
            if (false == jsonFile.toFile().exists()) {
                return TestImpactAnalysis.NOT_FOUND;
            }
            return TestImpactAnalysis.parse(Files.readString(jsonFile, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void saveTestImpactAnalysis(TestImpactAnalysis testImpactAnalysis) {
        try {
            var id = testImpactAnalysis.getId();
            var path = SkippyFolder.get(projectDir).resolve(Path.of("tia_%s.json".formatted(id)));
            Files.writeString(path, testImpactAnalysis.toJson(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            var idFile = SkippyFolder.get(projectDir).resolve(Path.of("LATEST"));
            Files.writeString(idFile, id, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            // deleteTemporaryExecutionDataFilesForCurrentBuild();
//            deleteObsoleteExecutionDataFiles(testImpactAnalysis);
            deleteObsoleteTestImpactAnalysisFiles(testImpactAnalysis);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String saveJacocoExecutionData(byte[] jacocoExecutionData) {
        try {
            var executionId = JacocoExecutionDataUtil.getExecutionId(jacocoExecutionData);
            Files.write(SkippyFolder.get(projectDir).resolve("%s.exec".formatted(executionId)), compress(jacocoExecutionData), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return executionId;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void saveTemporaryTestExecutionDataForCurrentBuild(String testClassName, byte[] executionData) {
        try {
            Files.write(SkippyFolder.get().resolve("%s.exec".formatted(testClassName)), executionData, CREATE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<Path> getTemporaryExecutionDataFilesForCurrentBuild() {
        var temporaryExecutionDataFiles = asList(SkippyFolder.get(projectDir).toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".exec")))
                .stream()
                .filter(file -> ! file.getName().matches("[A-Z0-9]{32}\\.exec"))
                .map(File::toPath).toList();
        return temporaryExecutionDataFiles;
    }

    private static byte[] compress(byte[] data) {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();

        byte[] buffer = new byte[1024];
        byte[] outputStream = new byte[0];

        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            byte[] newOutputStream = new byte[outputStream.length + count];
            System.arraycopy(outputStream, 0, newOutputStream, 0, outputStream.length);
            System.arraycopy(buffer, 0, newOutputStream, outputStream.length, count);
            outputStream = newOutputStream;
        }
        deflater.end();
        return outputStream;
    }

    private void deleteTemporaryExecutionDataFilesForCurrentBuild() {
        for (var executionDataFile : getTemporaryExecutionDataFilesForCurrentBuild()) {
            executionDataFile.toFile().delete();
        }
    }

    private void deleteObsoleteExecutionDataFiles(TestImpactAnalysis testImpactAnalysis) {
        var executions = testImpactAnalysis.getJacocoIds();
        var permanentExecutionDataFiles = asList(SkippyFolder.get(projectDir).toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".exec")))
                .stream()
                .map(File::toPath).toList();
        for (var executionDataFile : permanentExecutionDataFiles) {
            if (false == executions.contains(executionDataFile.toFile().getName().replaceAll("\\.exec", ""))) {
                executionDataFile.toFile().delete();
            }
        }
    }

    private void deleteObsoleteTestImpactAnalysisFiles(TestImpactAnalysis testImpactAnalysis) {
        var jsonFiles = asList(SkippyFolder.get(projectDir).toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".json")))
                .stream()
                .map(File::toPath).toList();
        for (var jsonFile : jsonFiles) {
            var jsonFileId = jsonFile.toFile().getName().substring(4, 4 + 32);
            if (false == testImpactAnalysis.getId().equals(jsonFileId)) {
                jsonFile.toFile().delete();
            }
        }
    }

}