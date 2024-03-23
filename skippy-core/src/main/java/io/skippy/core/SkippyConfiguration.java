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

import java.util.Objects;

/**
 * Skippy configuration.
 *
 * @author Florian McKee
 */
public class SkippyConfiguration {

    static final SkippyConfiguration DEFAULT = new SkippyConfiguration(false);

    private final boolean saveExecutionData;

    /**
     * C'tor.
     *
     * @param saveExecutionData {@code true} to save JaCoCo execution data for individual tests, {@code false}
     */
    public SkippyConfiguration(boolean saveExecutionData) {
        this.saveExecutionData = saveExecutionData;
    }

    /**
     * Returns {@code true} if JaCoCo execution data for individual tests will be saved, {@code false} otherwise.
     * <br /><br />
     * The purpose of this feature is to generate accurate test coverage reports despite tests being skipped.
     *
     * @return {@code true} if JaCoCo execution data for individual tests will be saved, {@code false} otherwise
     */
    boolean saveExecutionData() {
        return saveExecutionData;
    }

    /**
     * Creates a new instance from JSON.
     *
     * @param json the JSON representation of a {@link SkippyConfiguration}
     * @return a new instance from JSON
     */
    static SkippyConfiguration parse(String json) {
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
    String toJson() {
        return """
        {
            "saveExecutionData": "%s"
        }
        """.formatted(saveExecutionData);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkippyConfiguration that = (SkippyConfiguration) o;
        return saveExecutionData == that.saveExecutionData;
    }

    @Override
    public int hashCode() {
        return Objects.hash(saveExecutionData);
    }
}