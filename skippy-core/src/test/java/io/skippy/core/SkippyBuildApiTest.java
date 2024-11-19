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

import com.example.Bar;
import com.example.BarTest;
import com.example.Foo;
import com.example.FooTest;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.*;

public final class SkippyBuildApiTest {

    private ClassFileCollector classFileCollector;
    private SkippyRepository skippyRepository = mock(SkippyRepository.class);

    @BeforeEach
    void setup() {
        classFileCollector = () -> asList(
                new ClassFile("com.example.FooTest", Path.of("com/example/FooTest.class"), Path.of("build/classes/java/test"), "hash-foo-test"),
                new ClassFile("com.example.BarTest", Path.of("com/example/BarTest.class"), Path.of("build/classes/java/test"), "hash-bar-test"),
                new ClassFile("com.example.Foo", Path.of("com/example/Foo.class"), Path.of("build/classes/java/test"), "hash-foo"),
                new ClassFile("com.example.Bar", Path.of("com/example/Bar.class"), Path.of("build/classes/java/test"), "hash-bar")
        );
    }

    @Test
    void testBuildStartedSavesSkippyConfiguration() {
        var buildApi = new SkippyBuildApi(SkippyConfiguration.DEFAULT, classFileCollector, skippyRepository);
        buildApi.buildStarted();
        verify(skippyRepository).saveConfiguration(SkippyConfiguration.DEFAULT);
    }

    @Test
    void testEmptySkippyFolderWithoutExecFiles() throws JSONException {
        var buildApi = new SkippyBuildApi(SkippyConfiguration.DEFAULT, classFileCollector, skippyRepository);

        when(skippyRepository.readLatestTestImpactAnalysis()).thenReturn(TestImpactAnalysis.NOT_FOUND);
        buildApi.buildStarted();
        when(skippyRepository.getTestRecordings()).thenReturn(asList());

        var tiaCaptor = ArgumentCaptor.forClass(TestImpactAnalysis.class);
        buildApi.buildFinished();
        verify(skippyRepository).saveTestImpactAnalysis(tiaCaptor.capture());

        var tia = tiaCaptor.getValue();
        var expected = """
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
                "tests": []
            }
        """;
        JSONAssert.assertEquals(expected, tia.toJson(), JSONCompareMode.LENIENT);
    }

    @Test
    void testEmptySkippyFolderWithTwoExecFilesCoverageForSkippedTestsEnabled() throws JSONException {
        var config = new SkippyConfiguration(
            true,
            Optional.empty(),
            Optional.empty()
        );

        var buildApi = new SkippyBuildApi(config, classFileCollector, skippyRepository);

        when(skippyRepository.readLatestTestImpactAnalysis()).thenReturn(TestImpactAnalysis.NOT_FOUND);
        buildApi.buildStarted();

        when(skippyRepository.getTestRecordings()).thenReturn(asList(
            new TestRecording(
                "com.example.FooTest",
                Path.of("build/classes/java/test"),
                asList(TestTag.PASSED),
                asList(
                    ClassNameAndJaCoCoId.from(Foo.class),
                    ClassNameAndJaCoCoId.from(FooTest.class)
                ),
                "0xFOO".getBytes(StandardCharsets.UTF_8)
            ),
            new TestRecording(
                "com.example.BarTest",
                Path.of("build/classes/java/test"),
                asList(TestTag.PASSED),
                asList(
                    ClassNameAndJaCoCoId.from(Bar.class),
                    ClassNameAndJaCoCoId.from(BarTest.class)
                ),
                "0xBAR".getBytes(StandardCharsets.UTF_8)
            )
        ));

        when(skippyRepository.saveJacocoExecutionData("0xFOO".getBytes(StandardCharsets.UTF_8))).thenReturn("FOO");
        when(skippyRepository.saveJacocoExecutionData("0xBAR".getBytes(StandardCharsets.UTF_8))).thenReturn("BAR");

        var tiaCaptor = ArgumentCaptor.forClass(TestImpactAnalysis.class);
        buildApi.buildFinished();
        verify(skippyRepository).saveTestImpactAnalysis(tiaCaptor.capture());

        var tia = tiaCaptor.getValue();
        var expected = """
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
                        "class": 1,
                        "tags": ["PASSED"],
                        "coveredClasses": [0,1],
                        "executionId": "BAR"
                    },
                    {
                        "class": 3,
                        "tags": ["PASSED"],
                        "coveredClasses": [2,3],
                        "executionId": "FOO"
                    }
                ]
            }
        """;
        JSONAssert.assertEquals(expected, tia.toJson(), JSONCompareMode.LENIENT);
    }

    @Test
    void testEmptySkippyFolderWithTwoExecFiles() throws JSONException {
        var buildApi = new SkippyBuildApi(SkippyConfiguration.DEFAULT, classFileCollector, skippyRepository);

        when(skippyRepository.readLatestTestImpactAnalysis()).thenReturn(TestImpactAnalysis.NOT_FOUND);
        buildApi.buildStarted();

        when(skippyRepository.getTestRecordings()).thenReturn(asList(
                new TestRecording(
                    "com.example.FooTest",
                    Path.of("build/classes/java/test"),
                    asList(TestTag.PASSED),
                    asList(
                        ClassNameAndJaCoCoId.from(Foo.class),
                        ClassNameAndJaCoCoId.from(FooTest.class)
                    ),
                    "0xFOO".getBytes(StandardCharsets.UTF_8)
                ),
                new TestRecording(
                    "com.example.BarTest",
                    Path.of("build/classes/java/test"),
                    asList(TestTag.PASSED),
                    asList(
                        ClassNameAndJaCoCoId.from(Bar.class),
                        ClassNameAndJaCoCoId.from(BarTest.class)
                    ),
                    "0xBAR".getBytes(StandardCharsets.UTF_8)
                )
        ));

        var tiaCaptor = ArgumentCaptor.forClass(TestImpactAnalysis.class);
        buildApi.buildFinished();
        verify(skippyRepository).saveTestImpactAnalysis(tiaCaptor.capture());


        var tia = tiaCaptor.getValue();
        var expected = """
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
                        "class": 1,
                        "tags": ["PASSED"],
                        "coveredClasses": [0,1]
                    },
                    {
                        "class": 3,
                        "tags": ["PASSED"],
                        "coveredClasses": [2,3]
                    }
                ]
            }
        """;
        JSONAssert.assertEquals(expected, tia.toJson(), JSONCompareMode.LENIENT);
    }

    @Test
    void testEmptySkippyFolderWithTwoExecFilesAndTwoExecFiles() throws JSONException {
        var buildApi = new SkippyBuildApi(SkippyConfiguration.DEFAULT, classFileCollector, skippyRepository);

        when(skippyRepository.readLatestTestImpactAnalysis()).thenReturn(TestImpactAnalysis.NOT_FOUND);
        buildApi.buildStarted();

        when(skippyRepository.getTestRecordings()).thenReturn(asList(
            new TestRecording(
                "com.example.FooTest",
                Path.of("build/classes/java/test"),
                asList(TestTag.PASSED),
                asList(
                    ClassNameAndJaCoCoId.from(Foo.class),
                    ClassNameAndJaCoCoId.from(FooTest.class)
                ),
                "0xFOO".getBytes(StandardCharsets.UTF_8)
            ),
            new TestRecording(
                "com.example.BarTest",
                Path.of("build/classes/java/test"),
                asList(TestTag.PASSED),
                asList(
                    ClassNameAndJaCoCoId.from(Bar.class),
                    ClassNameAndJaCoCoId.from(BarTest.class)
                ),
                "0xBAR".getBytes(StandardCharsets.UTF_8)
            )
        ));

        var tiaCaptor = ArgumentCaptor.forClass(TestImpactAnalysis.class);
        buildApi.buildFinished();
        verify(skippyRepository).saveTestImpactAnalysis(tiaCaptor.capture());

        var tia = tiaCaptor.getValue();
        var expected =  """
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
                        "class": 1,
                        "tags": ["PASSED"],
                        "coveredClasses": [0,1]
                    },
                    {
                        "class": 3,
                        "tags": ["PASSED"],
                        "coveredClasses": [2,3]
                    }
                ]
            }
        """;
        JSONAssert.assertEquals(expected, tia.toJson(), JSONCompareMode.LENIENT);
    }

    @Test
    void testEmptySkippyFolderWithTwoExecFilesOneFailedTests() throws JSONException {
        var buildApi = new SkippyBuildApi(SkippyConfiguration.DEFAULT, classFileCollector, skippyRepository);

        when(skippyRepository.readLatestTestImpactAnalysis()).thenReturn(TestImpactAnalysis.NOT_FOUND);
        buildApi.buildStarted();

        when(skippyRepository.getTestRecordings()).thenReturn(asList(
            new TestRecording(
                "com.example.FooTest",
                Path.of("build/classes/java/test"),
                asList(TestTag.FAILED),
                asList(
                    ClassNameAndJaCoCoId.from(Foo.class)
                ),
                "0xFOO".getBytes(StandardCharsets.UTF_8)
            ),
            new TestRecording(
                "com.example.BarTest",
                Path.of("build/classes/java/test"),
                asList(TestTag.PASSED),
                asList(
                    ClassNameAndJaCoCoId.from(Bar.class)
                ),
                "0xBAR".getBytes(StandardCharsets.UTF_8)
            )
        ));

        var tiaCaptor = ArgumentCaptor.forClass(TestImpactAnalysis.class);
        buildApi.buildFinished();
        verify(skippyRepository).saveTestImpactAnalysis(tiaCaptor.capture());

        var tia = tiaCaptor.getValue();
        var expected = """
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
                        "class": 1,
                        "tags": ["PASSED"],
                        "coveredClasses": [0]
                    },
                    {
                        "class": 3,
                        "tags": ["FAILED"],
                        "coveredClasses": [2]
                    }
                ]
            }
        """;
        JSONAssert.assertEquals(expected, tia.toJson(), JSONCompareMode.LENIENT);
    }

    @Test
    void testExistingJsonFileNoExecFile() throws JSONException {
        var buildApi = new SkippyBuildApi(SkippyConfiguration.DEFAULT, classFileCollector, skippyRepository);

        when(skippyRepository.readLatestTestImpactAnalysis()).thenReturn(TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.Bar",
                        "path": "com/example/Bar.class",
                        "outputFolder": "build/classes/java/test", 
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
        var expected = """
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
        """;
        JSONAssert.assertEquals(expected, tia.toJson(), JSONCompareMode.LENIENT);
    }

    @Test
    void testExistingJsonFileUpdatedExecFile() throws JSONException {
        var buildApi = new SkippyBuildApi(SkippyConfiguration.DEFAULT, classFileCollector, skippyRepository);

        when(skippyRepository.readLatestTestImpactAnalysis()).thenReturn(TestImpactAnalysis.parse("""
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
                        "class": 0,
                        "tags": ["PASSED"],
                        "coveredClasses": [0]
                    }
                ]
            }
        """));

        buildApi.buildStarted();

        when(skippyRepository.getTestRecordings()).thenReturn(asList(
            new TestRecording(
                "com.example.FooTest",
                Path.of("build/classes/java/test"),
                asList(TestTag.PASSED),
                asList(
                    ClassNameAndJaCoCoId.from(Foo.class),
                    ClassNameAndJaCoCoId.from(FooTest.class)
                ),
                "0xFOO".getBytes(StandardCharsets.UTF_8)
            )
        ));

        var tiaCaptor = ArgumentCaptor.forClass(TestImpactAnalysis.class);
        buildApi.buildFinished();
        verify(skippyRepository).saveTestImpactAnalysis(tiaCaptor.capture());

        var tia = tiaCaptor.getValue();
        var expected = """
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
                    "class": 3,
                    "tags": ["PASSED"],
                    "coveredClasses": [2,3]
                }
             ]
         }
        """;
        JSONAssert.assertEquals(expected, tia.toJson(), JSONCompareMode.LENIENT);
    }

    @Test
    void testExistingJsonFileUpdatedExecFileExecutionDataEnabled() throws JSONException {
        var config = new SkippyConfiguration(
            true,
            Optional.empty(),
            Optional.empty()
        );
        var buildApi = new SkippyBuildApi(config, classFileCollector, skippyRepository);

        when(skippyRepository.readLatestTestImpactAnalysis()).thenReturn(TestImpactAnalysis.parse("""
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
                        "coveredClasses": ["0"],
                        "executionId": "00000000000000000000000000000000"
                    }
                ]
            }
        """));

        buildApi.buildStarted();

        when(skippyRepository.getTestRecordings()).thenReturn(asList(
            new TestRecording(
                "com.example.FooTest",
                Path.of("build/classes/java/test"),
                asList(TestTag.PASSED),
                asList(
                    ClassNameAndJaCoCoId.from(Foo.class),
                    ClassNameAndJaCoCoId.from(FooTest.class)
                ),
                "0xFOO".getBytes(StandardCharsets.UTF_8)
            )
        ));

        when(skippyRepository.saveJacocoExecutionData("0xFOO".getBytes(StandardCharsets.UTF_8))).thenReturn("11111111111111111111111111111111");

        var tiaCaptor = ArgumentCaptor.forClass(TestImpactAnalysis.class);
        buildApi.buildFinished();
        verify(skippyRepository).saveTestImpactAnalysis(tiaCaptor.capture());

        var tia = tiaCaptor.getValue();
        var expected = """
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
                    "class": 3,
                    "tags": ["PASSED"],
                    "coveredClasses": [2,3],
                    "executionId": "11111111111111111111111111111111"
                }
             ]
         }
        """;
        JSONAssert.assertEquals(expected, tia.toJson(), JSONCompareMode.LENIENT);
    }

    @Test
    void testExistingJsonFileNewTestFailure() throws JSONException {
        var buildApi = new SkippyBuildApi(SkippyConfiguration.DEFAULT, classFileCollector, skippyRepository);

        when(skippyRepository.readLatestTestImpactAnalysis()).thenReturn(TestImpactAnalysis.parse("""
            {
                "classes": {
                    "0": {
                        "name": "com.example.Bar",
                        "path": "com/example/Bar.class",
                        "outputFolder": "build/classes/java/test",
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
                        "outputFolder": "build/classes/java/test",
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
                        "class": 1,
                        "tags": ["PASSED"],
                        "coveredClasses": [0,1]
                    },
                    {
                        "class": 3,
                        "tags": ["PASSED"],
                        "coveredClasses": [2,3]
                    }
                ]
            }
        """));

        buildApi.buildStarted();

        when(skippyRepository.getTestRecordings()).thenReturn(asList(
            new TestRecording(
                "com.example.FooTest",
                Path.of("build/classes/java/test"),
                asList(TestTag.FAILED),
                asList(
                    ClassNameAndJaCoCoId.from(Foo.class),
                    ClassNameAndJaCoCoId.from(FooTest.class)
                ),
                "0xFOO".getBytes(StandardCharsets.UTF_8)
            )
        ));

        var tiaCaptor = ArgumentCaptor.forClass(TestImpactAnalysis.class);
        buildApi.buildFinished();
        verify(skippyRepository).saveTestImpactAnalysis(tiaCaptor.capture());

        var tia = tiaCaptor.getValue();
        var expected = """
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
                        "class": 1,
                        "tags": ["PASSED"],
                        "coveredClasses": [0,1]
                    },
                    {
                        "class": 3,
                        "tags": ["FAILED"],
                        "coveredClasses": [2,3]
                    }
                ]
            }
        """;
        JSONAssert.assertEquals(expected, tia.toJson(), JSONCompareMode.LENIENT);
    }

}