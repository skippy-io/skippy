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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.nio.file.Files.*;
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
        skippyRepository = SkippyRepository.getInstance(SkippyConfiguration.DEFAULT, projectDir, null);
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
        var config = new SkippyConfiguration(
            true,
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
        );
        skippyRepository.saveConfiguration(config);
        var content = readString(configFile, StandardCharsets.UTF_8);
        assertThat(content).isEqualToIgnoringWhitespace("""
            {
                "coverageForSkippedTests": "true",
                "repositoryExtension": "io.skippy.core.DefaultRepositoryExtension",
                "predictionModifier": "io.skippy.core.DefaultPredictionModifier",
                "classFileSelector": "io.skippy.core.DefaultClassFileSelector"
            }
        """);
    }

    @Test
    void testSaveCustomConfiguration() throws IOException {
        var configFile = skippyFolder.resolve("config.json");
        assertFalse(exists(skippyFolder.resolve("config.json")));
        var config = new SkippyConfiguration(
                false,
                Optional.of("com.example.RepositoryExtension"),
                Optional.of("com.example.PredictionModifier"),
                Optional.of("com.example.ClassFileSelector")
        );
        skippyRepository.saveConfiguration(config);
        var content = readString(configFile, StandardCharsets.UTF_8);
        assertThat(content).isEqualToIgnoringWhitespace("""
            {
                "coverageForSkippedTests": "false",
                "repositoryExtension": "com.example.RepositoryExtension",
                "predictionModifier": "com.example.PredictionModifier",
                "classFileSelector": "com.example.ClassFileSelector"
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
        skippyRepository.saveTemporaryJaCoCoExecutionDataForCurrentBuild("com.example.LeftPadderTest", "00000000", readAllBytes(execFile));
        assertTrue(exists(skippyFolder.resolve("com.example.LeftPadderTest.00000000.exec")));
    }

    @Test
    void testReadTemporaryJaCoCoExecutionDataForCurrentBuild() throws Exception {
        var execFile = Paths.get(getClass().getResource("com.example.LeftPadderTest.exec").toURI());
        skippyRepository.saveTemporaryJaCoCoExecutionDataForCurrentBuild("com.example.LeftPadderTest", "00000000", readAllBytes(execFile));
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

    @Test
    void testSaveAndReadJaCoCoExecutionData() throws Exception {
        var executionData = Files.readAllBytes(Paths.get(getClass().getResource("com.example.LeftPadderTest.exec").toURI()));
        var id = skippyRepository.saveJacocoExecutionData(executionData);
        assertEquals("D40016DC6B856D89EA17DB14F370D026", id);
        assertArrayEquals(executionData, skippyRepository.readJacocoExecutionData(id).get());
    }

    @Test
    void testSaveTestImpactAnalysis() throws IOException {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.FooTest",
                        "path": "com/example/FooTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "ZT0GoiWG8Az5TevH9/JwBg=="
                    }
                },
                "tests": [
                    {
                        "class": "0",
                        "tags": ["PASSED"],
                        "coveredClasses": ["0"]
                    }
                ]
            }
        """);
        skippyRepository.saveTestImpactAnalysis(testImpactAnalysis);

        var tiaJson = skippyFolder.resolve("test-impact-analysis.json");
        assertTrue(exists(tiaJson));
        assertThat(readString(tiaJson, StandardCharsets.UTF_8)).isEqualToIgnoringWhitespace(testImpactAnalysis.toJson());

        var latest = skippyFolder.resolve("LATEST");
        assertTrue(exists(latest));
        assertThat(readString(latest, StandardCharsets.UTF_8)).isEqualTo("4BF8006482E1196644540C5E3979F3B2");
    }

    @Test
    void readLatestTestImpactAnalysis_latest_file_not_found() {
        var testImpactAnalysis = skippyRepository.readLatestTestImpactAnalysis();
        assertEquals(testImpactAnalysis, TestImpactAnalysis.NOT_FOUND);
    }

    @Test
    void readLatestTestImpactAnalysis_json_file_not_found() throws IOException {
        writeString(skippyFolder.resolve("LATEST"), "D013368C0DD441D819DEA78640F4EC1A", StandardCharsets.UTF_8);
        var testImpactAnalysis = skippyRepository.readLatestTestImpactAnalysis();
        assertEquals(testImpactAnalysis, TestImpactAnalysis.NOT_FOUND);
    }

    @Test
    void readLatestTestImpactAnalysis_json_file_does_not_match_version_in_latest() throws IOException {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.FooTest",
                        "path": "com/example/FooTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "ZT0GoiWG8Az5TevH9/JwBg=="
                    }
                },
                "tests": [
                    {
                        "class": "0",
                        "tags": ["PASSED"],
                        "coveredClasses": ["0"]
                    }
                ]
            }
        """);
        writeString(skippyFolder.resolve("LATEST"), "00000000000000000000000000000000", StandardCharsets.UTF_8);
        writeString(skippyFolder.resolve("test-impact-analysis.json"), testImpactAnalysis.toJson(), StandardCharsets.UTF_8);
        assertEquals(TestImpactAnalysis.NOT_FOUND, skippyRepository.readLatestTestImpactAnalysis());
    }
    @Test
    void readLatestTestImpactAnalysis_json_file_does_match_version_in_latest() throws IOException {
        var testImpactAnalysis = TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.FooTest",
                        "path": "com/example/FooTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "ZT0GoiWG8Az5TevH9/JwBg=="
                    }
                },
                "tests": [
                    {
                        "class": "0",
                        "tags": ["PASSED"],
                        "coveredClasses": ["0"]
                    }
                ]
            }
        """);
        writeString(skippyFolder.resolve("LATEST"), testImpactAnalysis.getId(), StandardCharsets.UTF_8);
        writeString(skippyFolder.resolve("test-impact-analysis.json"), testImpactAnalysis.toJson(), StandardCharsets.UTF_8);
        assertEquals(testImpactAnalysis, skippyRepository.readLatestTestImpactAnalysis());
    }

    @Test
    void testDeleteTags() {
        skippyRepository.tagTest("com.example.FooTest", TestTag.FAILED);
        assertTrue(exists(skippyFolder.resolve("tags.txt")));
        skippyRepository.deleteTestTags();
        assertFalse(exists(skippyFolder.resolve("tags.txt")));
    }

    @Test
    void testTagging()  {
        // a test is considered PASSED until it is tagged as FAILED
        assertEquals(asList(TestTag.PASSED), skippyRepository.getTestTags("com.example.Test1"));
        skippyRepository.tagTest("com.example.Test1", TestTag.FAILED);
        assertEquals(asList(TestTag.FAILED), skippyRepository.getTestTags("com.example.Test1"));
    }

    @Test
    void testTaggingAlwaysRun()  {
        // a test is considered PASSED until it is tagged as FAILED
        assertEquals(asList(TestTag.PASSED), skippyRepository.getTestTags("com.example.Test1"));
        skippyRepository.tagTest("com.example.Test1", TestTag.ALWAYS_EXECUTE);
        assertEquals(asList(TestTag.PASSED, TestTag.ALWAYS_EXECUTE), skippyRepository.getTestTags("com.example.Test1"));
    }

}