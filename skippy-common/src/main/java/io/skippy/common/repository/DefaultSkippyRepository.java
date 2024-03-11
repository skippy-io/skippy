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
import io.skippy.common.model.TestImpactAnalysis;
import io.skippy.common.util.JacocoExecutionDataUtil;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.Deflater;

import static java.util.Arrays.asList;

/**
 * {@link SkippyRepository} that stores and retrieves all data in / from the .skippy folder. This implementation only
 * retains the latest {@link TestImpactAnalysis} and the JaCoCo execution data files that are referenced by it.
 *
 * It is useful for small projects that do not care about code coverage reports and thus not need to store JaCoCo
 * execution data files. However, it supports storage of {@link TestImpactAnalysis} instances and JaCoCo execution data
 * files of any size. This is useful for experimentation. It is not recommended to be used for large projects and/or
 * projects that want to store JaCoCo execution data files since it might significantly increase the size of your Git
 * repository.
 *
 * Large projects that want to store and retain {@link TestImpactAnalysis}s instances and JaCoCo execution data files
 * should provide a custom implementation to stores those artefacts outside the project's repository.
 *
 * @author Florian McKee
 */
class DefaultSkippyRepository extends AbstractSkippyRepository {

    DefaultSkippyRepository(Path projectDir) {
        super(projectDir);
    }

    @Override
    public TestImpactAnalysis readTestImpactAnalysis() {
        var idFile = SkippyFolder.get(projectDir).resolve(Path.of("LATEST"));
        if (false == idFile.toFile().exists()) {
            return TestImpactAnalysis.NOT_FOUND;
        }
        try {
            var jsonFile = SkippyFolder.get(projectDir).resolve(Path.of("test-impact-analysis.json"));
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
            var jsonFile = SkippyFolder.get(projectDir).resolve(Path.of("test-impact-analysis.json"));
            Files.writeString(jsonFile, testImpactAnalysis.toJson(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            var idFile = SkippyFolder.get(projectDir).resolve(Path.of("LATEST"));
            Files.writeString(idFile, id, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            deleteTemporaryExecutionDataFilesForCurrentBuild();
            deleteObsoleteExecutionDataFiles(testImpactAnalysis);
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

}