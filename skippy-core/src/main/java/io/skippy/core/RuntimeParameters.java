package io.skippy.core;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * Parameters that are passed from Skippy's build plugins to the JUnit libraries.
 */
public class RuntimeParameters {

    /**
     * A parameter that is passed from Skippy's build plugins to the JUnit libraries.
     */
    public enum Parameter {

        /**
         * The name of the test task (e.g., test, integrationTest, etc.) that is currently running.
         */
        TEST_TASK("skippy.testtask", "test");

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

    private RuntimeParameters(Map<Parameter, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * Creates a new instance from all system properties that match one of the known parameters.
     *
     * @return a new instance from all system properties that match one of the known parameters
     */
    public static RuntimeParameters fromSystemProperties() {
        var result = new HashMap<Parameter, String>();
        for (var parameter : Parameter.values()) {
            result.put(parameter, System.getProperties().getProperty(parameter.systemPropertyKey, parameter.defaultValue));
        }
        return new RuntimeParameters(result);
    }

    String get(Parameter parameter) {
        return parameters.getOrDefault(parameter, parameter.defaultValue);
    }

}
