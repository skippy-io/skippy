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

import java.util.*;

import static java.lang.System.lineSeparator;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;

/**
 * A test that has been analyzed by Skippy.
 * <br /><br />
 * JSON example:
 * <pre>
 * {
 *      "class": 0,
 *      "tags": ["PASSED"],
 *      "coveredClasses": [0, 1],
 *      "executionId": "C57F877F...."
 * }
 * </pre>
 *
 * See {@link TestImpactAnalysis} for an overview how {@link AnalyzedTest} fits into Skippy's data model.
 *
 * @author Florian McKee
 */
final class AnalyzedTest implements Comparable<AnalyzedTest> {

    private final int testClassId;
    private final List<TestTag> tags;
    private final List<Integer> coveredClassesIds;
    private final Optional<String> executionId;

    /**
     * C'tor.
     *
     * @param testClassId the id of the test class in the {@link ClassFileContainer}
     * @param tags a list of {@link TestTag}s
     * @param coveredClassesIds the ids of the covered classes in the {@link ClassFileContainer}
     * @param executionId a unique identifier for the test's JaCoCo execution data if capture of execution data is enabled
     */
    AnalyzedTest(int testClassId, List<TestTag> tags, List<Integer> coveredClassesIds, Optional<String> executionId) {
        this.testClassId = testClassId;
        this.tags = tags;
        this.coveredClassesIds = coveredClassesIds;
        this.executionId = executionId;
    }

    static AnalyzedTest from(ClassFileContainer classFileContainer, ClassFile classFile, List<TestTag> tags, List<ClassFile> coveredClasses, Optional<String> executionId) {
        return new AnalyzedTest(
                classFileContainer.getId(classFile),
                tags,
                coveredClasses.stream().map(classFileContainer::getId).toList(),
                executionId
        );
    }

    /**
     * Returns the id of the test class in the {@link ClassFileContainer}.
     *
     * @return the id of the test class in the {@link ClassFileContainer}
     */
    int getTestClassId() {
        return testClassId;
    }


    /**
     * Returns the ids of the covered classes in the {@link ClassFileContainer}.
     *
     * @return the ids of the covered classes in the {@link ClassFileContainer}
     */
    List<Integer> getCoveredClassesIds() {
        return coveredClassesIds;
    }

    /**
     * Returns a unique identifier for the test's JaCoCo execution data if capture of execution data is enabled.
     *
     * @return a unique identifier for the test's JaCoCo execution data if capture of execution data is enabled
     */
    Optional<String> getExecutionId() {
        return executionId;
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

    static AnalyzedTest parse(Tokenizer tokenizer) {
        tokenizer.skip('{');
        Integer clazz = null;
        List<Integer> coveredClasses = null;
        List<TestTag> testTags = null;
        Optional<String> executionId = Optional.empty();
        while (true) {
            var key = tokenizer.next();
            tokenizer.skip(':');
            switch (key) {
                case "class":
                    clazz = Integer.valueOf(tokenizer.next());
                    break;
                case "coveredClasses":
                    coveredClasses = parseCoveredClasses(tokenizer);
                    break;
                case "tags":
                    testTags = TestTag.parseList(tokenizer);
                    break;
                case "executionId":
                    executionId = Optional.of(tokenizer.next());
                    break;
            }
            tokenizer.skipIfNext(',');
            if (tokenizer.peek('}')) {
                tokenizer.skip('}');
                break;
            }
        }
        return new AnalyzedTest(clazz, testTags, coveredClasses, executionId);
    }

    static List<Integer> parseCoveredClasses(Tokenizer tokenizer) {
        var coveredClasses = new ArrayList<Integer>();
        tokenizer.skip('[');
        while ( ! tokenizer.peek(']')) {
            tokenizer.skipIfNext(',');
            coveredClasses.add(Integer.valueOf(tokenizer.next()));
        }
        tokenizer.skip(']');
        return coveredClasses;
    }

    List<TestTag> getTags() {
        return tags;
    }

    boolean isTaggedAs(TestTag tag) {
        return tags.contains(tag);
    }

    String toJson() {
        var result = new StringBuilder();
        result.append("\t\t{" + lineSeparator());
        result.append("\t\t\t\"class\": %s".formatted(getTestClassId()));
        result.append(",%s".formatted(lineSeparator()));
        result.append("\t\t\t\"tags\": [%s]".formatted(tags.stream()
                .sorted()
                .map(tag -> "\"%s\"".formatted(tag.name()))
                .collect(joining(","))));
        result.append(",%s".formatted(lineSeparator()));
        result.append("\t\t\t\"coveredClasses\": [%s]".formatted(getCoveredClassesIds().stream()
                .map(Integer::valueOf)
                .sorted()
                .map(val -> Integer.toString(val))
                .collect(joining(","))));
        if (executionId.isPresent()) {
            result.append(",%s".formatted(lineSeparator()));
            result.append("\t\t\t\"executionId\": \"%s\"".formatted(executionId.get()));
        }
        result.append(lineSeparator());
        result.append("\t\t}");
        return result.toString();
    }

    @Override
    public int compareTo(AnalyzedTest other) {
        return comparing(AnalyzedTest::getTestClassId).compare(this, other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalyzedTest that = (AnalyzedTest) o;
        return testClassId == that.testClassId &&
                Objects.equals(tags, that.tags) &&
                Objects.equals(coveredClassesIds, that.coveredClassesIds) &&
                Objects.equals(executionId, that.executionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testClassId, tags, coveredClassesIds, executionId);
    }
}