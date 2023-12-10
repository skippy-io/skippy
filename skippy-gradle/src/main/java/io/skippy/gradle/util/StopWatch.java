package io.skippy.gradle.util;

public class StopWatch {

    /**
     * Measures the runtime of the {@code runnable} in milliseconds.
     *
     * @param runnable
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
