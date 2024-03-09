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
import io.skippy.common.repository.DefaultSkippyRepository;
import io.skippy.common.model.JsonProperty;
import io.skippy.common.model.TestImpactAnalysis;
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

public final class SkippyBuildApiTest {

    private Path projectDir;
    private Path skippyFolder;
    private SkippyBuildApi buildApi;

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
        buildApi = new SkippyBuildApi(projectDir, classFileCollector, new DefaultSkippyRepository(projectDir));
        for (var file : skippyFolder.toFile().listFiles()) {
            file.delete();
        }
    }

    @Test
    void testEmptySkippyFolderWithoutCovFiles(){
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
    void testEmptySkippyFolderWithTwoCovFiles() throws IOException {
        buildApi.buildStarted();

        Files.writeString(skippyFolder.resolve("com.example.FooTest.cov"), """
            com.example.Foo
            com.example.FooTest
        """, StandardCharsets.UTF_8);

        Files.writeString(skippyFolder.resolve("com.example.BarTest.cov"), """
            com.example.Bar
            com.example.BarTest
        """, StandardCharsets.UTF_8);

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
                        "coveredClasses": ["0","1"]
                    },
                    {
                        "class": "3",
                        "result": "PASSED",
                        "coveredClasses": ["2","3"]
                    }
                ]
            }
    """);
    }

    @Test
    void testEmptySkippyFolderWithTwoCovFilesAndTwoExecFiles() throws IOException {
        buildApi.buildStarted();

        Files.writeString(skippyFolder.resolve("com.example.FooTest.cov"), """
            com.example.Foo
            com.example.FooTest
        """, StandardCharsets.UTF_8);

        Files.writeString(skippyFolder.resolve("com.example.BarTest.cov"), """
            com.example.Bar
            com.example.BarTest
        """, StandardCharsets.UTF_8);

        Files.writeString(skippyFolder.resolve("com.example.FooTest.exec"), """
            AAAAAAAAAA
        """, StandardCharsets.UTF_8);

        Files.writeString(skippyFolder.resolve("com.example.BarTest.exec"), """
            BBBBBBBBBB
        """, StandardCharsets.UTF_8);

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
                        "executionDataRef": "1E879EAB8A2218642C8FABF2F51740AF"
                    },
                    {
                        "class": "3",
                        "result": "PASSED",
                        "coveredClasses": ["2","3"],
                        "executionDataRef": "083AAD6F53062D78C57F877F6F9BF164"
                    }
                ]
            }
    """);
    }

    @Test
    void testEmptySkippyFolderWithTwoCovFilesOneFailedTests() throws IOException {
        buildApi.buildStarted();

        Files.writeString(skippyFolder.resolve("com.example.FooTest.cov"), """
            com.example.Foo
        """, StandardCharsets.UTF_8);

        Files.writeString(skippyFolder.resolve("com.example.BarTest.cov"), """
            com.example.Bar
        """, StandardCharsets.UTF_8);

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
                        "coveredClasses": ["0"]
                    },
                    {
                        "class": "3",
                        "result": "FAILED",
                        "coveredClasses": ["2"]
                    }
                ]
            }
        """);
    }

    @Test
    void testExistingJsonFileNoCovFile() throws IOException {
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
                        "coveredClasses": ["0"]
                    }
                ]
            }
        """, StandardCharsets.UTF_8);

        buildApi.buildStarted();

        Files.writeString(skippyFolder.resolve("com.example.FooTest.cov"), """
            com.example.Foo
            com.example.FooTest
        """, StandardCharsets.UTF_8);

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
                    "coveredClasses": ["2","3"]
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
                        "coveredClasses": ["0","1"]
                    },
                    {
                        "class": "3",
                        "result": "PASSED",
                        "coveredClasses": ["2","3"]
                    }
                ]
            }
        """, StandardCharsets.UTF_8);

        buildApi.buildStarted();

        Files.writeString(skippyFolder.resolve("com.example.FooTest.cov"), """
            com.example.Foo
            com.example.FooTest
        """, StandardCharsets.UTF_8);
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
                        "coveredClasses": ["0","1"]
                    },
                    {
                        "class": "3",
                        "result": "FAILED",
                        "coveredClasses": ["2","3"]
                    }
                ]
            }
        """);
    }
}