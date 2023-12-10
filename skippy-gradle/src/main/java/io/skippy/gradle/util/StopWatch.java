package io.skippy.gradle.util;

/**
 * Utility class to measure runtimes.
 */
public class StopWatch {

    /**
     * Measures the runtime of the {@code runnable} in milliseconds.
     *
     * @param runnable a {@link Runnable}
     * @return the runtime of the {@code runnable} in milliseconds
     */
    public static long measureInMs(Runnable runnable) {
        var then = System.currentTimeMillis();
        try {
            runnable.run();
        } finally {
            return System.currentTimeMillis() - then;
        }
    }

}
