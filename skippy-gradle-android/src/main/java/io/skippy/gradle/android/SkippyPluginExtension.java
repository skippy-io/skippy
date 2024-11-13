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

package io.skippy.gradle.android;

import io.skippy.core.SkippyConfiguration;
import org.gradle.api.provider.Property;

import java.util.Optional;

/**
 * Extension that allows configuration of Skippy in Gradle's build file:
 * <pre>
 * skippy {
 *     executionData = true
 *     ...
 * }
 * </pre>
 *
 * @author Florian McKee
 */
public interface SkippyPluginExtension  {

    /**
     * Returns the property to enable / disable coverage generation for skipped tests.
     *
     * @return the property to enable / disable coverage generation for skipped tests
     */
    Property<Boolean> getCoverageForSkippedTests();

    /**
     * Returns the property to register a custom {@link io.skippy.core.SkippyRepositoryExtension}.
     *
     * @return the property to register a custom {@link io.skippy.core.SkippyRepositoryExtension}
     */
    Property<String> getRepository();

    /**
     * Returns the property to register a custom {@link io.skippy.core.PredictionModifier}.
     *
     * @return the property to register a custom {@link io.skippy.core.PredictionModifier}
     */
    Property<String> getPredictionModifier();

    /**
     * Returns the property to register a custom {@link io.skippy.core.ClassFileSelector}.
     *
     * @return the property to register a custom {@link io.skippy.core.ClassFileSelector}
     */
    Property<String> getClassFileSelector();

    /**
     * Converts the extension data into a {@link SkippyConfiguration}
     *
     * @return a {@link SkippyConfiguration} derived from the extension data
     */
    default SkippyConfiguration toSkippyConfiguration() {
        return new SkippyConfiguration(
            getCoverageForSkippedTests().getOrElse(false),
            Optional.ofNullable(getRepository().getOrNull()),
            Optional.ofNullable(getPredictionModifier().getOrNull()),
            Optional.ofNullable(getClassFileSelector().getOrNull())
        );
    }
}