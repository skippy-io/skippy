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

import io.skippy.core.SkippyFolder;
import io.skippy.core.SkippyConfiguration;
import io.skippy.core.SkippyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.readAllBytes;
import static java.util.Arrays.asList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class SkippyRepositoryTest {


    Path projectDir;
    SkippyRepository skippyRepository;
    Path skippyFolder;

    @BeforeEach
    void setUp() throws URISyntaxException {
        projectDir = Paths.get(getClass().getResource(".").toURI());
        skippyRepository = SkippyRepository.getInstance(new SkippyConfiguration(false), projectDir);
        skippyRepository.deleteSkippyFolder();
        skippyFolder = SkippyFolder.get(projectDir);
    }
    @Test
    void testDeleteSkippyFolder() {
        assertTrue(exists(skippyFolder));
        skippyRepository.deleteSkippyFolder();
        assertFalse(exists(skippyFolder));
    }

    @Test
    void testSaveConfiguration() throws IOException {
        var configFile = skippyFolder.resolve("config.json");
        assertFalse(exists(skippyFolder.resolve("config.json")));
        skippyRepository.saveConfiguration(new SkippyConfiguration(true));
        var content = Files.readString(configFile, StandardCharsets.UTF_8);
        assertThat(content).isEqualToIgnoringWhitespace("""
            {
                "saveExecutionData": "true"
            }
        """);
    }

    @Test
    void testDeleteLogFiles() throws IOException {
        var logFile = skippyFolder.resolve("predictions.log");
        Files.writeString(logFile, "TEXT", StandardCharsets.UTF_8);
        assertTrue(exists(logFile));
        skippyRepository.deleteLogFiles();
        assertFalse(exists(logFile));
    }

    @Test
    void testSaveTemporaryJaCoCoExecutionDataForCurrentBuild() throws Exception {
        var execFile = Paths.get(getClass().getResource("com.example.LeftPadderTest.exec").toURI());
        assertFalse(exists(skippyFolder.resolve("com.example.LeftPadderTest.exec")));
        skippyRepository.saveTemporaryJaCoCoExecutionDataForCurrentBuild("com.example.LeftPadderTest", readAllBytes(execFile));
        assertTrue(exists(skippyFolder.resolve("com.example.LeftPadderTest.exec")));
    }

    @Test
    void testReadTemporaryJaCoCoExecutionDataForCurrentBuild() throws Exception {
        var execFile = Paths.get(getClass().getResource("com.example.LeftPadderTest.exec").toURI());
        skippyRepository.saveTemporaryJaCoCoExecutionDataForCurrentBuild("com.example.LeftPadderTest", readAllBytes(execFile));
        var resultSet = skippyRepository.readTemporaryJaCoCoExecutionDataForCurrentBuild();
        assertEquals(1, resultSet.size());
        var result = resultSet.get(0);
        assertEquals(
            asList(
                "com.example.LeftPadder",
                "com.example.LeftPadderTest",
                "com.example.StringUtils"
            ),
            result.coveredClasses().stream()
                .filter(className -> className.startsWith("com.example"))
                .sorted()
                .toList()
        );
        assertEquals("com.example.LeftPadderTest", result.testClassName());
        assertArrayEquals(readAllBytes(execFile), result.jacocoExecutionData());
    }

}