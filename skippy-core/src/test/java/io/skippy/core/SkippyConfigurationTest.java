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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SkippyConfigurationTest {

    @Test
    void testToJson1() {
        var configuration = new SkippyConfiguration(true, Optional.empty());
        assertThat(configuration.toJson()).isEqualToIgnoringWhitespace("""
            {
                "coverageForSkippedTests": "true",
                "repositoryClass": "io.skippy.core.DefaultRepositoryExtension"
            }
        """);
    }

    @Test
    void testToJson2() {
        var configuration = new SkippyConfiguration(true, Optional.of("com.example.CustomRepository"));
        assertThat(configuration.toJson()).isEqualToIgnoringWhitespace("""
            {
                "coverageForSkippedTests": "true",
                "repositoryClass": "com.example.CustomRepository"
            }
        """);
    }

    @Test
    void testParse() {
        var json = """
            {
                "coverageForSkippedTests": "true"
            }
        """;
        var configuration = SkippyConfiguration.parse(json);
        assertEquals(true, configuration.generateCoverageForSkippedTests());
        assertEquals(true, configuration.generateCoverageForSkippedTests());

    }


}