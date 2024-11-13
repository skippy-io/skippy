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

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * Parameters that are passed from Skippy's build plugins to the JUnit libraries.
 *
 * @author Florian McKee
 */
public final class ParametersFromBuildPlugin {

    /**
     * A parameter that is passed from Skippy's build plugins to the JUnit libraries.
     */
    public enum Parameter {

        /**
         * The name of the test task (e.g., test, integrationTest, etc.) that is currently running.
         */
        TEST_TASK_NAME("skippy.testtask.name", "test"),

        /**
         * The classpath of the test task that is currently running as colon-separated list of paths.
         */
        TEST_TASK_CLASSPATH("skippy.testtask.classpath", "");

        private final String systemPropertyKey;
        private final String defaultValue;

        Parameter(String systemPropertyKey, String defaultValue) {
            this.systemPropertyKey = systemPropertyKey;
            this.defaultValue = defaultValue;
        }

        /**
         * Returns the command-line argument to pass this parameter as JVM argument from the build plugins to the JUnit
         * libraries.
         *
         * @param value the parameter's value
         * @return the command-line argument to pass this parameter as JVM argument
         */
        public String asJvmArgument(String value) {
            return "-D%s=%s".formatted(systemPropertyKey, value);
        }
    }

    private final Map<Parameter, String> parameters;

    private ParametersFromBuildPlugin(Map<Parameter, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * Creates a new instance from all system properties that match one of the known parameters.
     *
     * @return a new instance from all system properties that match one of the known parameters
     */
    public static ParametersFromBuildPlugin fromSystemProperties() {
        var result = new HashMap<Parameter, String>();
        for (var parameter : Parameter.values()) {
            result.put(parameter, System.getProperties().getProperty(parameter.systemPropertyKey, parameter.defaultValue));
        }
        return new ParametersFromBuildPlugin(result);
    }

    String get(Parameter parameter) {
        return parameters.getOrDefault(parameter, parameter.defaultValue);
    }

    static ParametersFromBuildPlugin none() {
        return new ParametersFromBuildPlugin(emptyMap());
    }

}
