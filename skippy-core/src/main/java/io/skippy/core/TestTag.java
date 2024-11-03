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

import java.util.ArrayList;
import java.util.List;

/**
 * Tags that can be associated with a test.
 *
 * @author Florian McKee
 */
public enum TestTag {

    /**
     * The test was successful.
     */
    PASSED,

    /**
     * The test failed.
     */
    FAILED,

    /**
     * The test must always execute (don't make {@link Prediction#SKIP} predictions).
     */
    ALWAYS_EXECUTE;

    static List<TestTag> parseList(Tokenizer tokenizer) {
        return Profiler.profile("TestTag#parseList", () -> {
            var testTags = new ArrayList<TestTag>();
            tokenizer.skip('[');
            while (!tokenizer.peek(']')) {
                tokenizer.skipIfNext(',');
                testTags.add(TestTag.valueOf(tokenizer.next()));
            }
            tokenizer.skip(']');
            return testTags;
        });
    }
}
