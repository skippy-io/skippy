/*
 * Copyright 2023 the original author or authors.
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

/**
 * A mapping between tests and the classes they cover.
 *
 * @author Florian McKee
 */
class TestImpactAnalysis {

    private static final Logger LOGGER = LogManager.getLogger(TestImpactAnalysis.class);

    static final TestImpactAnalysis UNAVAILABLE = new TestImpactAnalysis(emptyMap());

    private final Map<FullyQualifiedClassName, List<FullyQualifiedClassName>> testCoverage;

    /**
     * C'tor.
     *
     * @param testCoverage a mapping between tests and the classes that they cover
     */
    private TestImpactAnalysis(Map<FullyQualifiedClassName, List<FullyQualifiedClassName>> testCoverage) {
        this.testCoverage = testCoverage;
    }

    static TestImpactAnalysis parse(Path directory) {
        var result = new HashMap<FullyQualifiedClassName, List<FullyQualifiedClassName>>();
        for (var covFile : directory.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".cov"))) {
            var test = new FullyQualifiedClassName(toClassName(covFile.toPath()));
            result.put(test, parseCovFile(covFile.toPath()));
        }
        return new TestImpactAnalysis(result);
    }

    /**
     * Returns the classes that are covered by {@code test}.
     *
     * @param test a {@link FullyQualifiedClassName} representing a test
     * @return a list of {@link FullyQualifiedClassName}s representing the classes that are covered by {@code test}
     */
    List<FullyQualifiedClassName> getCoveredClasses(FullyQualifiedClassName test) {
        return testCoverage.getOrDefault(test, emptyList());
    }

    /**
     * Returns {@code true} if no coverage data is available for {@code test}, {@code false} otherwise.
     *
     * @param test a {@link FullyQualifiedClassName} representing a test
     * @return {@code true} if no coverage data is available for {@code test}, {@code false} otherwise
     */
    boolean noDataAvailableFor(FullyQualifiedClassName test) {
        return ! testCoverage.containsKey(test);
    }

    private static List<FullyQualifiedClassName> parseCovFile(Path csvFile) {
        try {
             return Files.readAllLines(csvFile, StandardCharsets.UTF_8).stream()
                     .map(line -> new FullyQualifiedClassName(line))
                     .toList();
        } catch (Exception e) {
            LOGGER.error("Parsing of file '%s' failed: '%s'".formatted(csvFile, e), e);
            throw new RuntimeException(e);
        }
    }

    private static String toClassName(Path covFile) {
        // /a/b/c/com.example.Foo.cov -> com.example.Foo.cov
        var filename = covFile.getFileName().toString();
        return filename.substring(0, filename.indexOf(".cov"));
    }

}