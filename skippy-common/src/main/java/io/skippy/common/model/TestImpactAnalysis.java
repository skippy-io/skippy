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

import io.skippy.common.SkippyFolder;
import io.skippy.common.util.Profiler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static io.skippy.common.SkippyConstants.TEST_IMPACT_ANALYSIS_JSON_FILE;
import static io.skippy.common.model.Reason.*;q
import static java.lang.System.lineSeparator;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;

/**
 * Programmatic representation of the `test-impact-analysis.json` file:
 *
 * <pre>
 * [
 *   {
 *     "test": "com.example.FooTest",
 *     ...
 *   },
 *   {
 *     "test": "com.example.BarTest",
 *     ...
 *   },
 *   ...
 * ]
 * </pre>
 *
 * @author Florian McKee
 */
public final class TestImpactAnalysis {

    private final List<AnalyzedTest> analyzedTests;

    /**
     * Creates a new instance.
     *
     * @param analyzedTests a list of {@link AnalyzedTest}s
     */
    public TestImpactAnalysis(List<AnalyzedTest> analyzedTests) {
        this.analyzedTests = analyzedTests;
    }

    /**
     * Creates a new instance based off the data in the Skippy folder.
     *
     * @return a new instance based off the JSON file in the Skippy folder
     */
    public static TestImpactAnalysis readFromSkippyFolder() {
        var testImpactAnalysisJsonFile = SkippyFolder.get().resolve(TEST_IMPACT_ANALYSIS_JSON_FILE);
        if ( ! testImpactAnalysisJsonFile.toFile().exists()) {
            return new TestImpactAnalysis(emptyList());
        }
        try {
            return parse(Files.readString(testImpactAnalysisJsonFile, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Makes a skip-or-execute prediction for the test identified by the {@code testClassName}.
     *
     * @param testClassName the test's fully-qualified class name (e.g., com.example.FooTest)
     * @return a skip-or-execute prediction for the test identified by the {@code testClassName}
     */
    public PredictionWithReason predict(String testClassName) {
        return Profiler.profile("TestImpactAnalysis#predict", () -> {
            var maybeAnalyzedTest = analyzedTests.stream().filter(test -> test.test().getClassName().equals(testClassName)).findFirst();
            if (maybeAnalyzedTest.isEmpty()) {
                return PredictionWithReason.execute(UNKNOWN_TEST);
            }
            var analyzedTest = maybeAnalyzedTest.get();

            if (analyzedTest.test().hasChanged()) {
                return PredictionWithReason.execute(BYTECODE_CHANGE_IN_TEST);
            }
            for (var coveredClass : analyzedTest.coveredClasses()) {
                if (coveredClass.hasChanged()) {
                    return PredictionWithReason.execute(BYTECODE_CHANGE_IN_COVERED_CLASS);
                }
            }
            return PredictionWithReason.skip(NO_CHANGE);
        });
    }

    List<AnalyzedTest> getAnalyzedTests() {
        return analyzedTests;
    }

    static TestImpactAnalysis parse(String string) {
        return parse(new Tokenizer(string));
    }

    static TestImpactAnalysis parse(Tokenizer tokenizer) {
        var tests = new ArrayList<AnalyzedTest>();
        tokenizer.skip("[");
        while ( ! tokenizer.peek("]")) {
            if (tokenizer.peek(",")) {
                tokenizer.skip(",");
            }
            tests.add(AnalyzedTest.parse(tokenizer));
        }
        tokenizer.skip("]");
        return new TestImpactAnalysis(tests);
    }

    /**
     * Returns a JSON representation of this instance.
     *
     * @return a JSON representation of this instance
     */
    public String toJson() {
        return """
            [
            %s
            ]""".formatted(analyzedTests.stream().sorted().map(c -> c.toJson()).collect(joining("," + lineSeparator()))
        );
    }

}