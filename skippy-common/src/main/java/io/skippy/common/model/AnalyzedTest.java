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
 * Represents a test that has been analyzed by Skippy.
 * <br /><br />
 * A list of {@link AnalyzedTest}s together with a {@link ClassFileContainer} make up a {@link TestImpactAnalysis}.
 * <br /><br />
 * JSON example:
 * <pre>
 * {
 *      "class": "0",
 *      "result": "PASSED",
 *      "coveredClasses": ["0", "1"],
 *      "executionId": "C57F877F6F9BF164"
 * }
 * </pre>
 *
 * @param testClassId the reference to the test class in the {@link ClassFileContainer}
 * @param result the {@link TestResult}
 * @param coveredClassesIds references to the covered classes in the {@link ClassFileContainer}
 * @param executionId a unique identifier for JaCoCo execution data if capture of execution data is enabled
 *                    (see {@link SkippyConfiguration#saveExecutionData()}), an empty {@link Optional} otherwise
 * @author Florian McKee
 */
public record AnalyzedTest(String testClassId, TestResult result, List<String> coveredClassesIds, Optional<String> executionId) implements Comparable<AnalyzedTest> {

    /**
     * Allows test to specify which properties to include in the JSON representation. This allows tests to focus on a
     * sub-set of all properties instead of asserting against the value of all properties.
     */
    public enum JsonProperty {

        /**
         * The reference to the test class in the {@link ClassFileContainer}.
         */
        AT_CLASS,

        /**
         * The {@link TestResult}.
         */
        AT_RESULT,

        /**
         * References to the covered classes in the {@link ClassFileContainer}.
         */
        AT_COVERED_CLASSES,

        /**
         * A unique identifier for JaCoCo execution data if capture of execution data is enabled.
         */
        AT_EXECUTION_ID;

        /**
         * Convenience method for tests that assert against a sub-set of the JSON representation.
         *
         * @param properties the input
         * @return the input
         */
        public static AnalyzedTest.JsonProperty[] testProperties(AnalyzedTest.JsonProperty... properties) {
            return properties;
        }

        /**
         * Convenience method for tests that assert against the entire JSON representation.
         *
         * @return all properties
         */
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
     * Returns this instance as JSON string.
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
            if (propertyToRender == JsonProperty.AT_EXECUTION_ID && executionId().isEmpty()) {
                continue;
            }
            renderedProperties.add(switch (propertyToRender) {
                case AT_CLASS -> "\t\t\t\"class\": \"%s\"".formatted(testClassId());
                case AT_RESULT -> "\t\t\t\"result\": \"%s\"".formatted(result());
                case AT_COVERED_CLASSES -> "\t\t\t\"coveredClasses\": [%s]".formatted(coveredClassesIds().stream()
                        .map(Integer::valueOf)
                        .sorted()
                        .map(id -> "\"%s\"".formatted(id)).collect(joining(",")));
                case AT_EXECUTION_ID -> "\t\t\t\"executionId\": \"%s\"".formatted(executionId.get());
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