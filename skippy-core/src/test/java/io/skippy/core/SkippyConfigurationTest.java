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

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SkippyConfigurationTest {

    @Test
    void testToJson1() {
        var configuration = new SkippyConfiguration(
            true,
            Optional.empty(),
            Optional.empty()
        );
        assertThat(configuration.toJson()).isEqualToIgnoringWhitespace("""
            {
                "coverageForSkippedTests": "true",
                "repositoryExtension": "io.skippy.core.DefaultRepositoryExtension",
                "predictionModifier": "io.skippy.core.DefaultPredictionModifier"
            }
        """);
    }

    @Test
    void testToJson2() {
        var configuration = new SkippyConfiguration(
                false,
                Optional.of("com.example.CustomRepository"),
                Optional.of("com.example.CustomModifier")
        );
        assertThat(configuration.toJson()).isEqualToIgnoringWhitespace("""
            {
                "coverageForSkippedTests": "false",
                "repositoryExtension": "com.example.CustomRepository",
                "predictionModifier": "com.example.CustomModifier"
            }
        """);
    }


    static class CustomRepository implements SkippyRepositoryExtension {

        public CustomRepository(Path path) {
        }

        @Override
        public Optional<TestImpactAnalysis> findTestImpactAnalysis(String id) {
            return Optional.empty();
        }

        @Override
        public void saveTestImpactAnalysis(TestImpactAnalysis testImpactAnalysis) {
        }

        @Override
        public Optional<byte[]> findJacocoExecutionData(String executionId) {
            return Optional.empty();
        }

        @Override
        public void saveJacocoExecutionData(String executionId, byte[] jacocoExecutionData) {
        }
    }

    static class CustomModifier implements PredictionModifier {

        public CustomModifier() {
        }

        @Override
        public PredictionWithReason passThruOrModify(Class<?> test, PredictionWithReason prediction) {
            return null;
        }
    }

    @Test
    void testParse() {
        var json = """
            {
                "coverageForSkippedTests": "true",
                "repositoryExtension": "io.skippy.core.SkippyConfigurationTest$CustomRepository",
                "predictionModifier": "io.skippy.core.SkippyConfigurationTest$CustomModifier"
            }
        """;
        var configuration = SkippyConfiguration.parse(json);
        assertEquals(true, configuration.generateCoverageForSkippedTests());
        assertEquals(CustomRepository.class, configuration.repositoryExtension(Path.of(".")).getClass());
        assertEquals(CustomModifier.class, configuration.predictionModifier().getClass());
    }

}