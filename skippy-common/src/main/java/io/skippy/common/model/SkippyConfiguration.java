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
 * @param saveExecutionData {@code true} if JaCoCo execution data for individual test should be persisted, {@code false}
 *
 * @author Florian McKee
 */
public record SkippyConfiguration(boolean saveExecutionData) {

    public static final SkippyConfiguration DEFAULT = new SkippyConfiguration(false);

    /**
     * Creates a new instance from JSON.
     *
     * @param json the JSON representation of a {@link SkippyConfiguration}
     * @return a new instance from JSON
     */
    public static SkippyConfiguration parse(String json) {
        var tokenizer = new Tokenizer(json);
        tokenizer.skip('{');
        boolean executionData = false;
        while (true) {
            var key = tokenizer.next();
            tokenizer.skip(':');
            switch (key) {
                case "saveExecutionData":
                    executionData = Boolean.valueOf(tokenizer.next());
                    break;
            }
            tokenizer.skipIfNext(',');
            if (tokenizer.peek('}')) {
                tokenizer.skip('}');
                break;
            }
        }
        return new SkippyConfiguration(executionData);
    }

    /**
     * Returns this instance as JSON string.
     *
     * @return the instance as JSON string
     */
    public String toJson() {
        return """
        {
            "saveExecutionData": "%s"
        }
        """.formatted(saveExecutionData);
    }
}