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

package io.skippy.common.model;

import io.skippy.common.util.Profiler;

import java.util.*;

import static io.skippy.common.model.AnalyzedTest.JsonProperty.allTestProperties;
import static java.lang.System.lineSeparator;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;

/**
 * Programmatic representation of a test in `test-impact-analysis.json`:
 *
 * <pre>
 * {
 *      "class": "0",
 *      "result": "PASSED",
 *      "coveredClasses": ["0", "1"],
 *      "execution": "C57F877F6F9BF164"
 * }
 * </pre>
 *
 * @author Florian McKee
 */
public record AnalyzedTest(String testClassId, TestResult result, List<String> coveredClassesIds, Optional<String> executionId) implements Comparable<AnalyzedTest> {

    public enum JsonProperty {
        CLASS,
        RESULT,
        COVERED_CLASSES,
        EXECUTION_ID;

        public static AnalyzedTest.JsonProperty[] testProperties(AnalyzedTest.JsonProperty... properties) {
            return Arrays.asList(properties).toArray(new AnalyzedTest.JsonProperty[0]);
        }

        public static AnalyzedTest.JsonProperty[] allTestProperties() {
            return JsonProperty.values();
        }
    }

    public AnalyzedTest(String testClassId, TestResult result, List<String> coveredClassesIds, String execution) {
        this(testClassId, result, coveredClassesIds, Optional.of(execution));
    }

    public AnalyzedTest(String testClassId, TestResult result, List<String> coveredClassesIds) {
        this(testClassId, result, coveredClassesIds, Optional.empty());
    }

    static AnalyzedTest parse(Tokenizer tokenizer) {
        tokenizer.skip('{');
        String clazz = null;
        List<String> coveredClasses = null;
        TestResult testResult = null;
        Optional<String> execution = Optional.empty();
        while (true) {
            var key = tokenizer.next();
            tokenizer.skip(':');
            switch (key) {
                case "class":
                    clazz = tokenizer.next();
                    break;
                case "coveredClasses":
                    coveredClasses = parseCoveredClasses(tokenizer);
                    break;
                case "result":
                    testResult = TestResult.valueOf(tokenizer.next());
                    break;
                case "executionId":
                    execution = Optional.of(tokenizer.next());
                    break;
            }
            tokenizer.skipIfNext(',');
            if (tokenizer.peek('}')) {
                tokenizer.skip('}');
                break;
            }
        }
        return new AnalyzedTest(clazz, testResult, coveredClasses, execution);
    }

    static List<AnalyzedTest> parseList(Tokenizer tokenizer) {
        return Profiler.profile("AnalyzedTest#parseList", () -> {
            var analyzedTests = new ArrayList<AnalyzedTest>();
            tokenizer.skip('[');
            while (!tokenizer.peek(']')) {
                tokenizer.skipIfNext(',');
                analyzedTests.add(parse(tokenizer));
            }
            tokenizer.skip(']');
            return analyzedTests;
        });
    }

    private static List<String> parseCoveredClasses(Tokenizer tokenizer) {
        var coveredClasses = new ArrayList<String>();
        tokenizer.skip('[');
        while ( ! tokenizer.peek(']')) {
            tokenizer.skipIfNext(',');
            coveredClasses.add(tokenizer.next());
        }
        tokenizer.skip(']');
        return coveredClasses;
    }

    /**
     * Renders this instance as JSON string.
     *
     * @return the instance as JSON string
     */
    String toJson() {
        return toJson(allTestProperties());
    }

    /**
     * Renders this instance as JSON string.
     *
     * @param propertiesToRender the properties that should be rendered (rendering only a sub-set is useful for testing)
     * @return the instance as JSON string
     */
    String toJson(JsonProperty... propertiesToRender) {
        var result = new StringBuilder();
        result.append("\t\t{" + lineSeparator());
        var renderedProperties = new ArrayList<String>();

        for (var propertyToRender : propertiesToRender) {
            if (propertyToRender == JsonProperty.EXECUTION_ID && executionId().isEmpty()) {
                continue;
            }
            renderedProperties.add(switch (propertyToRender) {
                case CLASS -> "\t\t\t\"class\": \"%s\"".formatted(testClassId());
                case RESULT -> "\t\t\t\"result\": \"%s\"".formatted(result());
                case COVERED_CLASSES -> "\t\t\t\"coveredClasses\": [%s]".formatted(coveredClassesIds().stream()
                        .map(Integer::valueOf)
                        .sorted()
                        .map(id -> "\"%s\"".formatted(id)).collect(joining(",")));
                case EXECUTION_ID -> "\t\t\t\"executionId\": \"%s\"".formatted(executionId.get());
            });
        }
        result.append(renderedProperties.stream().collect(joining("," +  lineSeparator())));
        result.append(lineSeparator());
        result.append("\t\t}");
        return result.toString();
    }

    @Override
    public int compareTo(AnalyzedTest other) {
        return comparing(AnalyzedTest::testClassId).compare(this, other);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof AnalyzedTest a) {
            return Objects.equals(testClassId, a.testClassId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(testClassId);
    }

}