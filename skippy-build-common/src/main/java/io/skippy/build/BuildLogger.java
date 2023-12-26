package io.skippy.build;

/**
 * Logger that will logs using Gradle's or Maven's native build logger.
 */
public interface BuildLogger {

    /**
     * Logs the {@code message}.
     * @param message a message
     */
    void log(String message);
}
