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
import java.nio.file.Paths;
import java.util.Optional;
import java.nio.file.Path;

import static java.nio.file.Files.writeString;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class CustomRepositoryExtensionTest {

    static SkippyRepositoryExtension extensionMock = mock(SkippyRepositoryExtension.class);

    static class Extension implements SkippyRepositoryExtension {

        public Extension(Path projectDir) {
        }

        @Override
        public Optional<TestImpactAnalysis> findTestImpactAnalysis(String id) {
            return extensionMock.findTestImpactAnalysis(id);
        }

        @Override
        public void saveTestImpactAnalysis(TestImpactAnalysis testImpactAnalysis) {
            extensionMock.saveTestImpactAnalysis(testImpactAnalysis);
        }

        @Override
        public Optional<byte[]> findJacocoExecutionData(String executionId) {
            return extensionMock.findJacocoExecutionData(executionId);
        }

        @Override
        public void saveJacocoExecutionData(String executionId, byte[] jacocoExecutionData) {
            extensionMock.saveJacocoExecutionData(executionId, jacocoExecutionData);
        }
    }

    Path projectDir;
    SkippyRepository skippyRepository;
    Path skippyFolder;

    @BeforeEach
    void setUp() throws URISyntaxException {
        projectDir = Paths.get(getClass().getResource(".").toURI());
        skippyRepository = SkippyRepository.getInstance(new SkippyConfiguration(false, Optional.of(Extension.class.getName()), Optional.empty()), projectDir, null);
        skippyRepository.deleteSkippyFolder();
        skippyFolder = SkippyFolder.get(projectDir);
        reset(extensionMock);
    }

    @Test
    void testFindTestImpactAnalysis() throws IOException {
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
                        "result": "PASSED",
                        "coveredClasses": ["0"]
                    }
                ]
            }
        """);
        writeString(skippyFolder.resolve("LATEST"), testImpactAnalysis.getId(), StandardCharsets.UTF_8);

        when(extensionMock.findTestImpactAnalysis(testImpactAnalysis.getId())).thenReturn(Optional.of(testImpactAnalysis));

        assertEquals(testImpactAnalysis, skippyRepository.readLatestTestImpactAnalysis()); ;
    }

    @Test
    void testSaveTestImpactAnalysis() {
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
                        "result": "PASSED",
                        "coveredClasses": ["0"]
                    }
                ]
            }
        """);

        skippyRepository.saveTestImpactAnalysis(testImpactAnalysis);
        verify(extensionMock).saveTestImpactAnalysis(testImpactAnalysis);
    }

    @Test
    void testSaveJacocoExecutionData() throws Exception {
        var executionData = Files.readAllBytes(Paths.get(getClass().getResource("com.example.LeftPadderTest.exec").toURI()));
        skippyRepository.saveJacocoExecutionData(executionData);
        verify(extensionMock).saveJacocoExecutionData(JacocoUtil.getExecutionId(executionData), executionData);
    }

    @Test
    void testReadJacocoExecutionData() {
        when(extensionMock.findJacocoExecutionData("executionId")).thenReturn(Optional.of("bla".getBytes(StandardCharsets.UTF_8)));
        assertArrayEquals("bla".getBytes(StandardCharsets.UTF_8), skippyRepository.readJacocoExecutionData("executionId").get());
        verify(extensionMock).findJacocoExecutionData("executionId");
    }

}