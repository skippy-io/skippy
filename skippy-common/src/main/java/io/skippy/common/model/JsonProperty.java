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

import java.util.function.Function;

/**
 * Allows tests to specify a sub-set of all properties when rendering a {@link TestImpactAnalysis} to JSON. This makes
 * them less brittle to unrelated changes.
 *
 * @author Florian McKee
 */
public enum JsonProperty {
    /**
     * The class name.
     */
    CLASS_NAME("name", ClassFile::getClassName),

    /**
     * The path to the class file relative to the output folder.
     */
    CLASS_FILE("path", ClassFile::getClassFile),

    /**
     * The path of the output folder relative to the project root.
     */
    OUTPUT_FOLDER("outputFolder", ClassFile::getOutputFolder),

    /**
     * The hash of the class file.
     */
    HASH("hash", ClassFile::getHash);

    final String propertyName;
    final Function<ClassFile, Object> propertyValueProvider;

    JsonProperty(String propertyName, Function<ClassFile, Object> propertyValueProvider) {
        this.propertyName = propertyName;
        this.propertyValueProvider = propertyValueProvider;
    }
}
