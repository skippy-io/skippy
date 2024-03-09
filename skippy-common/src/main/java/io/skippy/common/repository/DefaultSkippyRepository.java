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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.Deflater;

import static io.skippy.common.SkippyConstants.TEST_IMPACT_ANALYSIS_JSON_FILE;
import static io.skippy.common.util.HashUtil.hashWith32Digits;


/**
 * {@link SkippyRepository} that stores and retrieves all data in / from the .skippy folder.
 */
class DefaultSkippyRepository implements SkippyRepository {

    private final Path projectDir;

    DefaultSkippyRepository(Path projectDir) {
        this.projectDir = projectDir;
    }

    @Override
    public String saveTestImpactAnalysis(TestImpactAnalysis testImpactAnalysis) {
        try {
            Files.writeString(SkippyFolder.get(projectDir).resolve(TEST_IMPACT_ANALYSIS_JSON_FILE), testImpactAnalysis.toJson(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return hashWith32Digits(testImpactAnalysis.toJson().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String saveJacocoExecutionData(byte[] jacocoExecutionData) {
        try {
            var hash = hashWith32Digits(jacocoExecutionData);
            Files.write(SkippyFolder.get(projectDir).resolve("%s.exec".formatted(hash)), compress(jacocoExecutionData), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return hash;
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

}
