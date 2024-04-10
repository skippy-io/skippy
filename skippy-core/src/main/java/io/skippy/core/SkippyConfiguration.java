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
import java.util.Optional;

/**
 * Skippy configuration that is used both by Skippy's build plugins and Skippy's JUnit libaries.
 *
 * @author Florian McKee
 */
public class SkippyConfiguration {

    static final SkippyConfiguration DEFAULT = new SkippyConfiguration(false, Optional.empty());

    private final boolean generateCoverageForSkippedTests;
    private final String repositoryClass;

    /**
     * C'tor.
     *
     * @param generateCoverageForSkippedTests {@code true} to generate coverage for skipped tests, {@code false} otherwise
     * @param repositoryClass the fully-qualified class name of the {@link SkippyRepositoryExtension} implementation for
     *                        this build or {@link Optional#empty()} if Skippy should use its default implementation
     */
    public SkippyConfiguration(boolean generateCoverageForSkippedTests, Optional<String> repositoryClass) {
        this.generateCoverageForSkippedTests = generateCoverageForSkippedTests;
        this.repositoryClass = repositoryClass.orElse(DefaultRepositoryExtension.class.getName());
    }

    /**
     * Returns {@code true} if Skippy should generate coverage for skipped tests, {@code false} otherwise.
     *
     * @return {@code true} if Skippy should generate coverage for skipped tests, {@code false} otherwise
     */
    boolean generateCoverageForSkippedTests() {
        return generateCoverageForSkippedTests;
    }

    /**
     * Returns the fully-qualified class name of the {@link SkippyRepositoryExtension} implementation for this build.
     *
     * @return the fully-qualified class name of the {@link SkippyRepositoryExtension} implementation for this build
     */
    String repositoryClass() {
        return repositoryClass;
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
        boolean coverageForSkippedTests = false;
        Optional<String> repositoryClass = Optional.empty();
        while (true) {
            var key = tokenizer.next();
            tokenizer.skip(':');
            switch (key) {
                case "coverageForSkippedTests":
                    coverageForSkippedTests = Boolean.valueOf(tokenizer.next());
                    break;
                case "repositoryClass":
                    repositoryClass = Optional.of(tokenizer.next());
                    break;
            }
            tokenizer.skipIfNext(',');
            if (tokenizer.peek('}')) {
                tokenizer.skip('}');
                break;
            }
        }
        return new SkippyConfiguration(coverageForSkippedTests, repositoryClass);
    }

    /**
     * Returns this instance as JSON string.
     *
     * @return the instance as JSON string
     */
    String toJson() {
        return """
        {
            "coverageForSkippedTests": "%s",
            "repositoryClass": "%s"
        }
        """.formatted(generateCoverageForSkippedTests, repositoryClass);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkippyConfiguration that = (SkippyConfiguration) o;
        return generateCoverageForSkippedTests == that.generateCoverageForSkippedTests && Objects.equals(repositoryClass, that.repositoryClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(generateCoverageForSkippedTests, repositoryClass);
    }
}