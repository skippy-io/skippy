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

/**
 * Extension point that allows projects to customize predictions made by Skippy.
 *
 * Example use cases:
 * <ul>
 *     <li>disable Skippy for all tests that carry a custom annotation,</li>
 *     <li>disable Skippy for all tests within a package,</li>
 *     <li>etc.</li>
 * </ul>
 *
 * <br /><br />
 * Custom implementations must be registered using Skippy's build plugins.
 * <br /><br />
 * Gradle example:
 * <pre>
 * skippy {
 *     ...
 *     predictionModifier = 'com.example.CustomPredictionModifier'
 * }
 * </pre>
 *
 * @author Florian McKee
 */
public interface PredictionModifier {

    /**
     * Returns a modified or unmodified prediction made by Skippy.
     *
     * @param test a class object representing a test
     * @param parametersFromBuildPlugin parameters that have been passed from Skippy's build plugin
     * @param prediction the prediction made by Skippy.
     * @return the modified or unmodified prediction
     */
    PredictionWithReason passThruOrModify(Class<?> test, ParametersFromBuildPlugin parametersFromBuildPlugin, PredictionWithReason prediction);

}