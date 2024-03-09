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

package io.skippy.build;

import io.skippy.common.SkippyFolder;
import io.skippy.common.model.JsonProperty;
import io.skippy.common.model.TestImpactAnalysis;
import io.skippy.common.repository.SkippyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.skippy.common.SkippyConstants.TEST_IMPACT_ANALYSIS_JSON_FILE;
import static io.skippy.common.model.ClassFile.fromParsedJson;
import static java.util.Arrays.asList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SkippyBuildApiTest {

    private Path projectDir;
    private Path skippyFolder;
    private SkippyBuildApi buildApi;
    private JacocoExecutionFileReader execFileReader = mock(JacocoExecutionFileReader.class);

    @BeforeEach
    void setup() throws URISyntaxException {
        ClassFileCollector classFileCollector = () -> asList(
                fromParsedJson("com.example.FooTest", Path.of("build/classes/java/test"), Path.of("com/example/FooTest.class"), "hash-foo-test"),
                fromParsedJson("com.example.BarTest", Path.of("build/classes/java/test"), Path.of("com/example/BarTest.class"), "hash-bar-test"),
                fromParsedJson("com.example.Foo", Path.of("build/classes/java/main"), Path.of("com/example/Foo.class"), "hash-foo"),
                fromParsedJson("com.example.Bar", Path.of("build/classes/java/main"), Path.of("com/example/Bar.class"), "hash-bar")
        );

        projectDir = Paths.get(getClass().getResource("project").toURI());
        skippyFolder = SkippyFolder.get(projectDir);
        buildApi = new SkippyBuildApi(projectDir, classFileCollector, SkippyRepository.getInstance(projectDir), execFileReader);
        for (var file : skippyFolder.toFile().listFiles()) {
            file.delete();
        }
    }

    @Test
    void testEmptySkippyFolderWithoutExecFiles(){
        buildApi.buildStarted();
        when(execFileReader.getJacocoExecutionDataFiles(projectDir)).thenReturn(asList());
        buildApi.buildFinished();

        var tia = TestImpactAnalysis.readFromFile(projectDir.resolve(".skippy").resolve(TEST_IMPACT_ANALYSIS_JSON_FILE));
        assertThat(tia.toJson(JsonProperty.CLASS_NAME)).isEqualToIgnoringWhitespace("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.Bar"
                    },
                    "1": {
                        "name": "com.example.BarTest"
                    },
                    "2": {
                        "name": "com.example.Foo"
                    },
                    "3": {
                        "name": "com.example.FooTest"
                    }
                },
                "tests": [
             
                ]
            }
        """);
    }

    @Test
    void testEmptySkippyFolderWithTwoCovFiles() {
        buildApi.buildStarted();

        var fooTestExecFile = skippyFolder.resolve("com.example.FooTest.exec");
        var barTestExecFile = skippyFolder.resolve("com.example.BarTest.exec");

        when(execFileReader.getJacocoExecutionData(fooTestExecFile)).thenReturn("0xFOO".getBytes(StandardCharsets.UTF_8));
        when(execFileReader.getJacocoExecutionData(barTestExecFile)).thenReturn("0xBAR".getBytes(StandardCharsets.UTF_8));

        when(execFileReader.getJacocoExecutionDataFiles(projectDir)).thenReturn(asList(
                fooTestExecFile,
                barTestExecFile
        ));

        when(execFileReader.getCoveredClasses(skippyFolder.resolve(fooTestExecFile))).thenReturn(asList(
                "com.example.Foo",
                "com.example.FooTest"
        ));

        when(execFileReader.getCoveredClasses(skippyFolder.resolve(barTestExecFile))).thenReturn(asList(
                "com.example.Bar",
                "com.example.BarTest"
        ));

        buildApi.buildFinished();

        var tia = TestImpactAnalysis.readFromFile(projectDir.resolve(".skippy").resolve(TEST_IMPACT_ANALYSIS_JSON_FILE));
        assertThat(tia.toJson(JsonProperty.CLASS_NAME)).isEqualToIgnoringWhitespace("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.Bar"
                    },
                    "1": {
                        "name": "com.example.BarTest"
                    },
                    "2": {
                        "name": "com.example.Foo"
                    },
                    "3": {
                        "name": "com.example.FooTest"
                    }
                },
                "tests": [
                    {
                        "class": "1",
                        "result": "PASSED",
                        "coveredClasses": ["0","1"],
                        "jacocoRef": "D358B7BF254A49F3EE2527EEE951B5BA"
                    },
                    {
                        "class": "3",
                        "result": "PASSED",
                        "coveredClasses": ["2","3"],
                        "jacocoRef": "C7A520851517A2B4F0677AE3CD9D8AFF"
                    }
                ]
            }
    """);
    }

    @Test
    void testEmptySkippyFolderWithTwoCovFilesAndTwoExecFiles() throws IOException {
        buildApi.buildStarted();

        var fooTestExecFile = skippyFolder.resolve("com.example.FooTest.exec");
        var barTestExecFile = skippyFolder.resolve("com.example.BarTest.exec");

        when(execFileReader.getJacocoExecutionData(fooTestExecFile)).thenReturn("0xFOO".getBytes(StandardCharsets.UTF_8));
        when(execFileReader.getJacocoExecutionData(barTestExecFile)).thenReturn("0xBAR".getBytes(StandardCharsets.UTF_8));

        when(execFileReader.getJacocoExecutionDataFiles(projectDir)).thenReturn(asList(
                fooTestExecFile,
                barTestExecFile
        ));

        when(execFileReader.getCoveredClasses(skippyFolder.resolve(fooTestExecFile))).thenReturn(asList(
                "com.example.Foo",
                "com.example.FooTest"
        ));

        when(execFileReader.getCoveredClasses(skippyFolder.resolve(barTestExecFile))).thenReturn(asList(
                "com.example.Bar",
                "com.example.BarTest"
        ));

        buildApi.buildFinished();

        var tia = TestImpactAnalysis.readFromFile(projectDir.resolve(".skippy").resolve(TEST_IMPACT_ANALYSIS_JSON_FILE));
        assertThat(tia.toJson(JsonProperty.CLASS_NAME)).isEqualToIgnoringWhitespace("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.Bar"
                    },
                    "1": {
                        "name": "com.example.BarTest"
                    },
                    "2": {
                        "name": "com.example.Foo"
                    },
                    "3": {
                        "name": "com.example.FooTest"
                    }
                },
                "tests": [
                    {
                        "class": "1",
                        "result": "PASSED",
                        "coveredClasses": ["0","1"],
                        "jacocoRef": "D358B7BF254A49F3EE2527EEE951B5BA"
                    },
                    {
                        "class": "3",
                        "result": "PASSED",
                        "coveredClasses": ["2","3"],
                        "jacocoRef": "C7A520851517A2B4F0677AE3CD9D8AFF"
                    }
                ]
            }
    """);
    }

    @Test
    void testEmptySkippyFolderWithTwoCovFilesOneFailedTests()  {
        buildApi.buildStarted();

        var fooTestExecFile = skippyFolder.resolve("com.example.FooTest.exec");
        var barTestExecFile = skippyFolder.resolve("com.example.BarTest.exec");

        when(execFileReader.getJacocoExecutionData(fooTestExecFile)).thenReturn("0xFOO".getBytes(StandardCharsets.UTF_8));
        when(execFileReader.getJacocoExecutionData(barTestExecFile)).thenReturn("0xBAR".getBytes(StandardCharsets.UTF_8));

        when(execFileReader.getJacocoExecutionDataFiles(projectDir)).thenReturn(asList(
                fooTestExecFile,
                barTestExecFile
        ));

        when(execFileReader.getCoveredClasses(skippyFolder.resolve(fooTestExecFile))).thenReturn(asList(
                "com.example.Foo"
        ));

        when(execFileReader.getCoveredClasses(skippyFolder.resolve(barTestExecFile))).thenReturn(asList(
                "com.example.Bar"
        ));

        buildApi.testFailed("com.example.FooTest");

        buildApi.buildFinished();

        var tia = TestImpactAnalysis.readFromFile(projectDir.resolve(".skippy").resolve(TEST_IMPACT_ANALYSIS_JSON_FILE));
        assertThat(tia.toJson(JsonProperty.CLASS_NAME)).isEqualToIgnoringWhitespace("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.Bar"
                    },
                    "1": {
                        "name": "com.example.BarTest"
                    },
                    "2": {
                        "name": "com.example.Foo"
                    },
                    "3": {
                        "name": "com.example.FooTest"
                    }
                },
                "tests": [
                    {
                        "class": "1",
                        "result": "PASSED",
                        "coveredClasses": ["0"],
                        "jacocoRef": "D358B7BF254A49F3EE2527EEE951B5BA"
                    },
                    {
                        "class": "3",
                        "result": "FAILED",
                        "coveredClasses": ["2"],
                        "jacocoRef": "C7A520851517A2B4F0677AE3CD9D8AFF"
                    }
                ]
            }
        """);
    }

    @Test
    void testExistingJsonFileNoExecFile() throws IOException {
        Files.writeString(skippyFolder.resolve(TEST_IMPACT_ANALYSIS_JSON_FILE), """
            {
                "classes": {
                    "0": {
                        "name": "com.example.Bar",
                        "path": "com/example/Bar.class",
                        "outputFolder": "build/classes/java/main",
                        "hash": "hash-bar"
                    }
                },
                "tests": [
                ]
            }
        """, StandardCharsets.UTF_8);

        buildApi.buildStarted();
        buildApi.buildFinished();

        var tia = TestImpactAnalysis.readFromFile(projectDir.resolve(".skippy").resolve(TEST_IMPACT_ANALYSIS_JSON_FILE));
        assertThat(tia.toJson(JsonProperty.CLASS_NAME)).isEqualToIgnoringWhitespace("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.Bar"
                    },
                    "1": {
                        "name": "com.example.BarTest"
                    },
                    "2": {
                        "name": "com.example.Foo"
                    },
                    "3": {
                        "name": "com.example.FooTest"
                    }
                },
                "tests": [
                ]
            }
        """);
    }

    @Test
    void testExistingJsonFileUpdatedCovFile() throws IOException {
        Files.writeString(skippyFolder.resolve(TEST_IMPACT_ANALYSIS_JSON_FILE), """
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
                        "coveredClasses": ["0"],
                        "jacocoRef": "00000000000000000000000000000000"
                    }
                ]
            }
        """, StandardCharsets.UTF_8);

        buildApi.buildStarted();

        var fooTestExecFile = skippyFolder.resolve("com.example.FooTest.exec");

        when(execFileReader.getJacocoExecutionData(fooTestExecFile)).thenReturn("0xFOO".getBytes(StandardCharsets.UTF_8));

        when(execFileReader.getJacocoExecutionDataFiles(projectDir)).thenReturn(asList(
                fooTestExecFile
        ));

        when(execFileReader.getCoveredClasses(skippyFolder.resolve(fooTestExecFile))).thenReturn(asList(
                "com.example.Foo",
                "com.example.FooTest"
        ));

        buildApi.buildFinished();

        var tia = TestImpactAnalysis.readFromFile(projectDir.resolve(".skippy").resolve(TEST_IMPACT_ANALYSIS_JSON_FILE));
        assertThat(tia.toJson(JsonProperty.CLASS_NAME)).isEqualToIgnoringWhitespace("""
        {
             "classes": {
                "0": {
                    "name": "com.example.Bar"
                },
                "1": {
                    "name": "com.example.BarTest"
                },
                "2": {
                    "name": "com.example.Foo"
                },
                "3": {
                    "name": "com.example.FooTest"
                }
             },
             "tests": [
                {
                    "class": "3",
                    "result": "PASSED",
                    "coveredClasses": ["2","3"],
                    "jacocoRef": "C7A520851517A2B4F0677AE3CD9D8AFF"
                }
             ]
         }
        """);
    }

    @Test
    void testExistingJsonFileNewTestFailure() throws IOException {
        Files.writeString(skippyFolder.resolve(TEST_IMPACT_ANALYSIS_JSON_FILE), """
            {
                "classes": {
                    "0": {
                        "name": "com.example.Bar",
                        "path": "com/example/Bar.class",
                        "outputFolder": "build/classes/java/main",
                        "hash": "hash-bar"
                    },
                    "1": {
                        "name": "com.example.BarTest",
                        "path": "com/example/BarTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "hash-bar-test"
                    },
                    "2": {
                        "name": "com.example.Foo",
                        "path": "com/example/Foo.class",
                        "outputFolder": "build/classes/java/main",
                        "hash": "hash-foo"
                    },
                    "3": {
                        "name": "com.example.FooTest",
                        "path": "com/example/FooTest.class",
                        "outputFolder": "build/classes/java/test",
                        "hash": "hash-foo-test"
                    },
                },
                "tests": [
                    {
                        "class": "1",
                        "result": "PASSED",
                        "coveredClasses": ["0","1"],
                        "jacocoRef": "11111111111111111111111111111111"
                    },
                    {
                        "class": "3",
                        "result": "PASSED",
                        "coveredClasses": ["2","3"],
                        "jacocoRef": "22222222222222222222222222222222"
                    }
                ]
            }
        """, StandardCharsets.UTF_8);

        buildApi.buildStarted();

        var fooTestExecFile = skippyFolder.resolve("com.example.FooTest.exec");

        when(execFileReader.getJacocoExecutionDataFiles(projectDir)).thenReturn(asList(fooTestExecFile));
        when(execFileReader.getJacocoExecutionData(fooTestExecFile)).thenReturn("0xFOO".getBytes(StandardCharsets.UTF_8));
        when(execFileReader.getCoveredClasses(skippyFolder.resolve(fooTestExecFile))).thenReturn(asList(
                "com.example.Foo",
                "com.example.FooTest"
        ));

        buildApi.testFailed("com.example.FooTest");

        buildApi.buildFinished();

        var tia = TestImpactAnalysis.readFromFile(projectDir.resolve(".skippy").resolve(TEST_IMPACT_ANALYSIS_JSON_FILE));
        assertThat(tia.toJson(JsonProperty.CLASS_NAME)).isEqualToIgnoringWhitespace("""
           {
                "classes": {
                    "0": {
                        "name": "com.example.Bar"
                    },
                    "1": {
                        "name": "com.example.BarTest"
                    },
                    "2": {
                        "name": "com.example.Foo"
                    },
                    "3": {
                        "name": "com.example.FooTest"
                    }
                },
                "tests": [
                    {
                        "class": "1",
                        "result": "PASSED",
                        "coveredClasses": ["0","1"],
                        "jacocoRef": "11111111111111111111111111111111"
                    },
                    {
                        "class": "3",
                        "result": "FAILED",
                        "coveredClasses": ["2","3"],
                        "jacocoRef": "C7A520851517A2B4F0677AE3CD9D8AFF"
                    }
                ]
            }
        """);
    }
}