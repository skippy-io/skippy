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

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;

/**
 * Programmatic representation of a test in `test-impact-analysis.json`:
 *
 * <pre>
 * {
 *      "class": "0",
 *      "result": "PASSED",
 *      "coveredClasses": ["0", "1"]
 * }
 * </pre>
 *
 * @author Florian McKee
 */
public record AnalyzedTest(String testClassId, TestResult result, List<String> coveredClassesIds, Optional<String> jacocoExecutionDataRef) implements Comparable<AnalyzedTest> {

    AnalyzedTest(String testClassId, TestResult result, List<String> coveredClassesIds, String jacocoExecutionDataRef) {
        this(testClassId, result, coveredClassesIds, Optional.of(jacocoExecutionDataRef));
    }

    AnalyzedTest(String testClassId, TestResult result, List<String> coveredClassesIds) {
        this(testClassId, result, coveredClassesIds, Optional.empty());
    }

    static AnalyzedTest parse(Tokenizer tokenizer) {
        tokenizer.skip('{');
        String clazz = null;
        List<String> coveredClasses = null;
        TestResult testResult = null;
        Optional<String> executionDataRef = Optional.empty();
        while (clazz == null || coveredClasses == null || testResult == null || executionDataRef.isEmpty()) {
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
                case "executionDataRef":
                    executionDataRef = Optional.of(tokenizer.next());
                    break;
            }
            if (clazz == null || coveredClasses == null || testResult == null || executionDataRef.isEmpty()) {
                tokenizer.skipIfNext(',');
            }
            if (tokenizer.peek('}')) {
                break;
            }
        }
        tokenizer.skip('}');
        return new AnalyzedTest(clazz, testResult, coveredClasses, executionDataRef);
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
        var coveredClassIdList = coveredClassesIds.stream()
                .map(Integer::valueOf)
                .sorted()
                .map(id -> "\"%s\"".formatted(id)).collect(joining(","));
        var result = new StringBuffer();
        result.append("\t\t{" + System.lineSeparator());
        result.append("\t\t\t\"class\": \"%s\",".formatted(testClassId) + System.lineSeparator());
        result.append("\t\t\t\"result\": \"%s\",".formatted(this.result) + System.lineSeparator());
        if (jacocoExecutionDataRef.isPresent()) {
            result.append("\t\t\t\"coveredClasses\": [%s],".formatted(coveredClassIdList) + System.lineSeparator());
            result.append("\t\t\t\"executionDataRef\": \"%s\"".formatted(this.jacocoExecutionDataRef.get()) + System.lineSeparator());
        } else {
            result.append("\t\t\t\"coveredClasses\": [%s]".formatted(coveredClassIdList) + System.lineSeparator());
        }
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