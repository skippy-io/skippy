/*
 * Copyright 2023 the original author or authors.
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

package io.skippy.gradle.model;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * Provides access to the properties in src/main/resources/skippy.properties.
 *
 * @author Florian McKee
 */
public class SkippyProperties {

    private static final Properties SKIPPY_PROPERTIES = read();

    /**
     * Returns the value of the {@code jacoco.version} property.
     *
     * @return the value of the {@code jacoco.version} property
     */
    public static String getJacocoVersion() {
        return SKIPPY_PROPERTIES.getProperty("jacoco.version");
    }

    private static Properties read() {
        try (var inputStream = SkippyProperties.class.getResourceAsStream("/skippy.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
