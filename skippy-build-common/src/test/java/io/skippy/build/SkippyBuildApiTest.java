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

import io.skippy.common.model.TestWithJacocoExecutionDataAndCoveredClasses;
import io.skippy.common.model.JsonProperty;
import io.skippy.common.model.TestImpactAnalysis;
import io.skippy.common.repository.SkippyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.skippy.common.model.ClassFile.fromParsedJson;
import static java.util.Arrays.asList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

public final class SkippyBuildApiTest {

    private Path projectDir;
    private SkippyBuildApi buildApi;
    private SkippyRepository skippyRepository = mock(SkippyRepository.class);

    @BeforeEach
    void setup() throws URISyntaxException {
        ClassFileCollector classFileCollector = () -> asList(
                fromParsedJson("com.example.FooTest", Path.of("build/classes/java/test"), Path.of("com/example/FooTest.class"), "hash-foo-test"),
                fromParsedJson("com.example.BarTest", Path.of("build/classes/java/test"), Path.of("com/example/BarTest.class"), "hash-bar-test"),
                fromParsedJson("com.example.Foo", Path.of("build/classes/java/main"), Path.of("com/example/Foo.class"), "hash-foo"),
                fromParsedJson("com.example.Bar", Path.of("build/classes/java/main"), Path.of("com/example/Bar.class"), "hash-bar")
        );

        projectDir = Paths.get(getClass().getResource("project").toURI());
        buildApi = new SkippyBuildApi(projectDir, classFileCollector, skippyRepository);
    }

    @Test
    void testEmptySkippyFolderWithoutExecFiles() {
        when(skippyRepository.readTestImpactAnalysis()).thenReturn(TestImpactAnalysis.NOT_FOUND);
        buildApi.buildStarted();
        when(skippyRepository.getTemporaryTestExecutionDataForCurrentBuild()).thenReturn(asList());

        var tiaCaptor = ArgumentCaptor.forClass(TestImpactAnalysis.class);
        buildApi.buildFinished();
        verify(skippyRepository).saveTestImpactAnalysis(tiaCaptor.capture());

        var tia = tiaCaptor.getValue();
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
        when(skippyRepository.readTestImpactAnalysis()).thenReturn(TestImpactAnalysis.NOT_FOUND);
        buildApi.buildStarted();

        when(skippyRepository.getTemporaryTestExecutionDataForCurrentBuild()).thenReturn(asList(
            new TestWithJacocoExecutionDataAndCoveredClasses(
                "com.example.FooTest",
                "0xFOO".getBytes(StandardCharsets.UTF_8),
                asList("com.example.Foo", "com.example.FooTest")
            ),
            new TestWithJacocoExecutionDataAndCoveredClasses(
                "com.example.BarTest",
                "0xBAR".getBytes(StandardCharsets.UTF_8),
                asList("com.example.Bar", "com.example.BarTest")
            )
        ));

        when(skippyRepository.saveJacocoExecutionData("0xFOO".getBytes(StandardCharsets.UTF_8))).thenReturn("0xFOO");
        when(skippyRepository.saveJacocoExecutionData("0xBAR".getBytes(StandardCharsets.UTF_8))).thenReturn("0xBAR");

        var tiaCaptor = ArgumentCaptor.forClass(TestImpactAnalysis.class);
        buildApi.buildFinished();
        verify(skippyRepository).saveTestImpactAnalysis(tiaCaptor.capture());

        var tia = tiaCaptor.getValue();
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
                        "jacocoId": "0xBAR"
                    },
                    {
                        "class": "3",
                        "result": "PASSED",
                        "coveredClasses": ["2","3"],
                        "jacocoId": "0xFOO"
                    }
                ]
            }
    """);
    }

    @Test
    void testEmptySkippyFolderWithTwoCovFilesAndTwoExecFiles() {
        when(skippyRepository.readTestImpactAnalysis()).thenReturn(TestImpactAnalysis.NOT_FOUND);
        buildApi.buildStarted();

        when(skippyRepository.getTemporaryTestExecutionDataForCurrentBuild()).thenReturn(asList(
                new TestWithJacocoExecutionDataAndCoveredClasses(
                        "com.example.FooTest",
                        "0xFOO".getBytes(StandardCharsets.UTF_8),
                        asList("com.example.Foo", "com.example.FooTest")
                ),
                new TestWithJacocoExecutionDataAndCoveredClasses(
                        "com.example.BarTest",
                        "0xBAR".getBytes(StandardCharsets.UTF_8),
                        asList("com.example.Bar", "com.example.BarTest")
                )
        ));

        when(skippyRepository.saveJacocoExecutionData("0xFOO".getBytes(StandardCharsets.UTF_8))).thenReturn("0xFOO");
        when(skippyRepository.saveJacocoExecutionData("0xBAR".getBytes(StandardCharsets.UTF_8))).thenReturn("0xBAR");

        var tiaCaptor = ArgumentCaptor.forClass(TestImpactAnalysis.class);
        buildApi.buildFinished();
        verify(skippyRepository).saveTestImpactAnalysis(tiaCaptor.capture());

        var tia = tiaCaptor.getValue();
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
                        "jacocoId": "0xBAR"
                    },
                    {
                        "class": "3",
                        "result": "PASSED",
                        "coveredClasses": ["2","3"],
                        "jacocoId": "0xFOO"
                    }
                ]
            }
    """);
    }

    @Test
    void testEmptySkippyFolderWithTwoCovFilesOneFailedTests()  {
        when(skippyRepository.readTestImpactAnalysis()).thenReturn(TestImpactAnalysis.NOT_FOUND);
        buildApi.buildStarted();

        when(skippyRepository.getTemporaryTestExecutionDataForCurrentBuild()).thenReturn(asList(
                new TestWithJacocoExecutionDataAndCoveredClasses(
                        "com.example.FooTest",
                        "0xFOO".getBytes(StandardCharsets.UTF_8),
                        asList("com.example.Foo")
                ),
                new TestWithJacocoExecutionDataAndCoveredClasses(
                        "com.example.BarTest",
                        "0xBAR".getBytes(StandardCharsets.UTF_8),
                        asList("com.example.Bar")
                )
        ));

        buildApi.testFailed("com.example.FooTest");

        when(skippyRepository.saveJacocoExecutionData("0xFOO".getBytes(StandardCharsets.UTF_8))).thenReturn("0xFOO");
        when(skippyRepository.saveJacocoExecutionData("0xBAR".getBytes(StandardCharsets.UTF_8))).thenReturn("0xBAR");

        var tiaCaptor = ArgumentCaptor.forClass(TestImpactAnalysis.class);
        buildApi.buildFinished();
        verify(skippyRepository).saveTestImpactAnalysis(tiaCaptor.capture());

        var tia = tiaCaptor.getValue();
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
                        "jacocoId": "0xBAR"
                    },
                    {
                        "class": "3",
                        "result": "FAILED",
                        "coveredClasses": ["2"],
                        "jacocoId": "0xFOO"
                    }
                ]
            }
        """);
    }

    @Test
    void testExistingJsonFileNoExecFile() {
        when(skippyRepository.readTestImpactAnalysis()).thenReturn(TestImpactAnalysis.parse("""
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
        """));

        var tiaCaptor = ArgumentCaptor.forClass(TestImpactAnalysis.class);
        buildApi.buildFinished();
        verify(skippyRepository).saveTestImpactAnalysis(tiaCaptor.capture());

        var tia = tiaCaptor.getValue();
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
    void testExistingJsonFileUpdatedCovFile() {
        when(skippyRepository.readTestImpactAnalysis()).thenReturn(TestImpactAnalysis.parse("""
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
                        "jacocoId": "00000000000000000000000000000000"
                    }
                ]
            }
        """));

        buildApi.buildStarted();

        when(skippyRepository.getTemporaryTestExecutionDataForCurrentBuild()).thenReturn(asList(
                new TestWithJacocoExecutionDataAndCoveredClasses(
                        "com.example.FooTest",
                        "0xFOO".getBytes(StandardCharsets.UTF_8),
                        asList("com.example.Foo", "com.example.FooTest")
                )
        ));

        when(skippyRepository.saveJacocoExecutionData("0xFOO".getBytes(StandardCharsets.UTF_8))).thenReturn("0xFOO");

        var tiaCaptor = ArgumentCaptor.forClass(TestImpactAnalysis.class);
        buildApi.buildFinished();
        verify(skippyRepository).saveTestImpactAnalysis(tiaCaptor.capture());

        var tia = tiaCaptor.getValue();
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
                    "jacocoId": "0xFOO"
                }
             ]
         }
        """);
    }

    @Test
    void testExistingJsonFileNewTestFailure() {
        when(skippyRepository.readTestImpactAnalysis()).thenReturn(TestImpactAnalysis.parse("""
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
                        "jacocoId": "11111111111111111111111111111111"
                    },
                    {
                        "class": "3",
                        "result": "PASSED",
                        "coveredClasses": ["2","3"],
                        "jacocoId": "22222222222222222222222222222222"
                    }
                ]
            }
        """));

        buildApi.buildStarted();


        when(skippyRepository.getTemporaryTestExecutionDataForCurrentBuild()).thenReturn(asList(
                new TestWithJacocoExecutionDataAndCoveredClasses(
                        "com.example.FooTest",
                        "0xFOO".getBytes(StandardCharsets.UTF_8),
                        asList("com.example.Foo", "com.example.FooTest")
                )
        ));
        when(skippyRepository.saveJacocoExecutionData("0xFOO".getBytes(StandardCharsets.UTF_8))).thenReturn("0xFOO");


        buildApi.testFailed("com.example.FooTest");

        var tiaCaptor = ArgumentCaptor.forClass(TestImpactAnalysis.class);
        buildApi.buildFinished();
        verify(skippyRepository).saveTestImpactAnalysis(tiaCaptor.capture());

        var tia = tiaCaptor.getValue();
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
                        "jacocoId": "11111111111111111111111111111111"
                    },
                    {
                        "class": "3",
                        "result": "FAILED",
                        "coveredClasses": ["2","3"],
                        "jacocoId": "0xFOO"
                    }
                ]
            }
        """);
    }
}