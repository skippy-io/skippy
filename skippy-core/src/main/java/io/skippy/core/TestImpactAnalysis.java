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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
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

    private enum JACOCO_CSV_COLUMN {
        GROUP,PACKAGE,CLASS,INSTRUCTION_MISSED,INSTRUCTION_COVERED,BRANCH_MISSED,BRANCH_COVERED,LINE_MISSED,LINE_COVERED,COMPLEXITY_MISSED,COMPLEXITY_COVERED,METHOD_MISSED,METHOD_COVERED
    }

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
        for (var csvFile : directory.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"))) {
            var test = new FullyQualifiedClassName(toClassName(csvFile.toPath()));
            result.put(test, parseCsvFile(csvFile.toPath()));
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

    private static List<FullyQualifiedClassName> parseCsvFile(Path csvFile) {
        try {
            var content = new ArrayList<>(Files.readAllLines(csvFile));
            // remove header
            content.remove(0);
            var coveredClasses = new ArrayList<FullyQualifiedClassName>();
            for (var line : content) {
                coveredClasses.addAll(parseCsvLine(line));
            }
            return coveredClasses;
        } catch (Exception e) {
            LOGGER.error("Parsing of file '%s' failed: '%s'".formatted(csvFile, e), e);
            throw new RuntimeException(e);
        }
    }

    private static List<FullyQualifiedClassName> parseCsvLine(String line) {
        var csvColumns = line.split(",");
        var pkg = csvColumns[JACOCO_CSV_COLUMN.PACKAGE.ordinal()];
        var clazz = csvColumns[JACOCO_CSV_COLUMN.CLASS.ordinal()];

        boolean coverageDetected = false;
        for (var column : asList(
                JACOCO_CSV_COLUMN.INSTRUCTION_COVERED,
                JACOCO_CSV_COLUMN.BRANCH_COVERED,
                JACOCO_CSV_COLUMN.LINE_COVERED,
                JACOCO_CSV_COLUMN.COMPLEXITY_COVERED,
                JACOCO_CSV_COLUMN.METHOD_COVERED)) {
            if (parseInt(csvColumns[column.ordinal()]) > 0) {
                coverageDetected = true;
            }
        }

        if (coverageDetected) {
            return asList(new FullyQualifiedClassName(pkg + "." + clazz));
        }
        return emptyList();
    }

    private static String toClassName(Path csvFile) {

        // /a/b/c/com_example_Foo.csv -> com_example_Foo.csv
        var filename = csvFile.getFileName().toString();

        // com_example_Foo.csv -> com_example_Foo
        var withoutExtension = filename.substring(0, filename.length() - 4);

        // com_example_Foo -> com.example.Foo
        return withoutExtension.replaceAll("_", ".");
    }

}