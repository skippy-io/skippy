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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestImpactAnalysisParsePerformanceTest {

    @Test
    void testParse() throws URISyntaxException, IOException {
        var jsonFile = Paths.get(getClass().getResource("test-impact-analysis.json").toURI());
        var testImpactAnalysis = TestImpactAnalysis.parse(Files.readString(jsonFile, StandardCharsets.UTF_8));
        Profiler.printResults();
        assertEquals("ACB10843996699388C4DD7A841D42BD9", testImpactAnalysis.getId());
        assertEquals(2510, testImpactAnalysis.getClassFileContainer().getClassFiles().size());
        assertEquals(400, testImpactAnalysis.getAnalyzedTests().size());
    }

}