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

/**
 * Skippy configuration that is used by Skippy's build plugins and JUnit libraries.
 *
 * @param persistExecutionData {@code true} if JaCoCo execution data for individual test should be persisted, {@code false}
 *
 * @author Florian McKee
 */
public record SkippyConfiguration(boolean persistExecutionData) {

    static SkippyConfiguration parse(String string) {
        var tokenizer = new Tokenizer(string);
        tokenizer.skip('{');
        boolean persistExecutionData = false;
        while (true) {
            var key = tokenizer.next();
            tokenizer.skip(':');
            switch (key) {
                case "persistExecutionData":
                    persistExecutionData = Boolean.valueOf(tokenizer.next());
                    break;
            }
            tokenizer.skipIfNext(',');
            if (tokenizer.peek('}')) {
                tokenizer.skip('}');
                break;
            }
        }
        return new SkippyConfiguration(persistExecutionData);
    }

    public String toJson() {
        return """
        {
            "persistExecutionData": "%s"
        }
        """.formatted(persistExecutionData);
    }
}