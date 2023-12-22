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
 * A mapping between tests and the classes they cover derived from the {@code .cov} files in the skippy folder.
 *
 * @author Florian McKee
 */
class CoverageData {

    private static final Logger LOGGER = LogManager.getLogger(CoverageData.class);

    static final CoverageData UNAVAILABLE = new CoverageData(emptyMap());

    private final Map<FullyQualifiedClassName, List<FullyQualifiedClassName>> coverageData;

    /**
     * C'tor.
     *
     * @param coverageData a mapping between tests and the classes that they cover
     */
    private CoverageData(Map<FullyQualifiedClassName, List<FullyQualifiedClassName>> coverageData) {
        this.coverageData = coverageData;
    }

    static CoverageData parse(Path directory) {
        return Profiler.profile("CoverageData#parse", () -> {
            var result = new HashMap<FullyQualifiedClassName, List<FullyQualifiedClassName>>();
            for (var covFile : directory.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".cov"))) {
                var test = new FullyQualifiedClassName(toClassName(covFile.toPath()));
                result.put(test, parseCovFile(covFile.toPath()));
            }
            return new CoverageData(result);
        });
    }

    /**
     * Returns the classes that are covered by the {@code test}.
     *
     * @param test a test
     * @return the classes that are covered by the {@code test}.
     */
    List<FullyQualifiedClassName> getCoveredClasses(FullyQualifiedClassName test) {
        return Profiler.profile("CoverageData#getCoveredClasses", () -> {
            return coverageData.getOrDefault(test, emptyList());
        });
    }

    /**
     * Returns {@code true} if no coverage data is available for the {@code test}, {@code false} otherwise.
     *
     * @param test a test
     * @return {@code true} if no coverage data is available for the {@code test}, {@code false} otherwise
     */
    boolean noDataAvailableFor(FullyQualifiedClassName test) {
        return Profiler.profile("CoverageData#noDataAvailableFor", () -> {
            return !coverageData.containsKey(test);
        });
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