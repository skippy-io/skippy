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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.System.lineSeparator;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;

/**
 * Programmatic representation of a test in `test-impact-analysis.json`:
 *
 * <pre>
 *  {
 *      "test": "com.example.FooTest",
 *      "path": "com/example/FooTest.class",
 *      "outputFolder": "build/classes/java/test",
 *      "hash": "ZT0GoiWG8Az5TevH9/JwBg==",
 *      "result": "SUCCESS",
 *      "coveredClasses": [...]
 *  }
 * </pre>
 *
 * @author Florian McKee
 */
public record AnalyzedTest(ClassFile test, TestResult result, List<ClassFile> coveredClasses) implements Comparable<AnalyzedTest> {

    static AnalyzedTest parse(Tokenizer tokenizer) {
        tokenizer.skip("{");
        var entries = new HashMap<String, Object>();
        while (entries.size() < 3) {
            var key = tokenizer.next();
            tokenizer.skip(":");
            if ("testClass".equals(key)) {
                entries.put(key, ClassFile.parse(tokenizer));
            } else if ("coveredClasses".equals(key)) {
                entries.put(key, parseCoveredClasses(tokenizer));
            } else {
                entries.put(key, tokenizer.next());
            }
            
            if (entries.size() < 3) {
                tokenizer.skip(",");
            }
        }
        tokenizer.skip("}");
        return new AnalyzedTest(
            (ClassFile) entries.get("testClass"),
            TestResult.valueOf(entries.get("result").toString()),
            (List<ClassFile>) entries.get("coveredClasses")
        );
    }

    private static List<ClassFile> parseCoveredClasses(Tokenizer tokenizer) {
        var coveredClasses = new ArrayList<ClassFile>();
        tokenizer.skip("[");
        while ( ! tokenizer.peek("]")) {
            if (tokenizer.peek(",")) {
                tokenizer.skip(",");
            }
            coveredClasses.add(ClassFile.parse(tokenizer));
        }
        tokenizer.skip("]");
        return coveredClasses;
    }

    String toJson() {
        return """
            \t{
            \t\t"testClass": %s,
            \t\t"result": "%s",
            \t\t"coveredClasses": [
            %s
            \t\t]
            \t}""".formatted(
                test.toTestClassJson(),
                result,
                coveredClasses.stream().sorted().map(c -> c.toJson()).collect(joining("," + lineSeparator())
        ));
    }

    @Override
    public int compareTo(AnalyzedTest other) {
        return comparing(AnalyzedTest::test).compare(this, other);
    }
}
