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

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * Skippy's configuration that is used both by Skippy's build plugins and Skippy's JUnit libaries.
 *
 * @author Florian McKee
 */
public class SkippyConfiguration {

    static final SkippyConfiguration DEFAULT = new SkippyConfiguration(false, Optional.empty(), Optional.empty());

    private final boolean generateCoverageForSkippedTests;
    private final String repositoryExtensionClass;
    private final String predictionModifierClass;

    /**
     * C'tor.
     *
     * @param generateCoverageForSkippedTests {@code true} to generate coverage for skipped tests, {@code false} otherwise
     * @param repositoryExtensionClass the fully-qualified class name of the {@link SkippyRepositoryExtension} for this build
     * @param predictionModifierClass the fully-qualified class name of the {@link PredictionModifier} for this build
     */
    public SkippyConfiguration(
            boolean generateCoverageForSkippedTests,
            Optional<String> repositoryExtensionClass,
            Optional<String> predictionModifierClass
    ) {
        this.generateCoverageForSkippedTests = generateCoverageForSkippedTests;
        this.repositoryExtensionClass = repositoryExtensionClass.orElse(DefaultRepositoryExtension.class.getName());
        this.predictionModifierClass = predictionModifierClass.orElse(DefaultPredictionModifier.class.getName());
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
     * Returns the {@link SkippyRepositoryExtension} for this build.
     *
     * @return the {@link SkippyRepositoryExtension} for this build
     */
    SkippyRepositoryExtension repositoryExtension(Path projectDir) {
        try {
            Class<?> clazz = Class.forName(repositoryExtensionClass);
            Constructor<?> constructor = clazz.getConstructor(Path.class);
            return (SkippyRepositoryExtension) constructor.newInstance(projectDir);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create repository extension %s: %s.".formatted(repositoryExtensionClass, e), e);
        }
    }

    /**
     * Returns the {@link PredictionModifier} for this build.
     *
     * @return the {@link PredictionModifier} for this build
     */
    PredictionModifier predictionModifier() {
        try {
            Class<?> clazz = Class.forName(predictionModifierClass);
            Constructor<?> constructor = clazz.getConstructor();
            return (PredictionModifier) constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create prediction modifier %s: %s.".formatted(predictionModifierClass, e), e);
        }
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
        Optional<String> repositoryExtension = Optional.empty();
        Optional<String> predictionModifier = Optional.empty();
        while (true) {
            var key = tokenizer.next();
            tokenizer.skip(':');
            switch (key) {
                case "coverageForSkippedTests":
                    coverageForSkippedTests = Boolean.valueOf(tokenizer.next());
                    break;
                case "repositoryExtension":
                    repositoryExtension = Optional.of(tokenizer.next());
                    break;
                case "predictionModifier":
                    predictionModifier = Optional.of(tokenizer.next());
                    break;
            }
            tokenizer.skipIfNext(',');
            if (tokenizer.peek('}')) {
                tokenizer.skip('}');
                break;
            }
        }
        return new SkippyConfiguration(coverageForSkippedTests, repositoryExtension, predictionModifier);
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
            "repositoryExtension": "%s",
            "predictionModifier": "%s"
        }
        """.formatted(generateCoverageForSkippedTests, repositoryExtensionClass, predictionModifierClass);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkippyConfiguration that = (SkippyConfiguration) o;
        return generateCoverageForSkippedTests == that.generateCoverageForSkippedTests
                && Objects.equals(repositoryExtensionClass, that.repositoryExtensionClass)
                && Objects.equals(predictionModifierClass, that.predictionModifierClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(generateCoverageForSkippedTests, repositoryExtensionClass, predictionModifierClass);
    }
}