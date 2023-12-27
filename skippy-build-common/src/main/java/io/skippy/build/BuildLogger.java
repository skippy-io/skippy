package io.skippy.build;

/**
 * Abstraction that shields this project from having to know about build-tool specific loggers
 * (e.g., {@code org.gradle.api.logging.Logger}).
 */
public interface BuildLogger {

    /**
     * Logs the {@code message}.
     *
     * @param message a message
     */
    void log(String message);
}
