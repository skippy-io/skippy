package io.skippy.gradle;

import org.gradle.api.logging.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class Profiler {

    private static Map<String, AtomicLong> data = new ConcurrentHashMap<>();

    public static <T> T profile(Class profiledClass, String profiledMethod, Supplier<T> action) {
        var identifier = profiledClass.getName() + "#" + profiledMethod;
        if ( ! data.containsKey(identifier)) {
            data.put(identifier, new AtomicLong(0));
        }
        var then = System.currentTimeMillis();
        try {
            return action.get();
        } finally {
            var now = System.currentTimeMillis();
            data.get(identifier).addAndGet(now - then);
        }
    }

    public static void profile(Class profiledClass, String profiledMethod, Runnable action) {
        profile(profiledClass, profiledMethod, () -> {
            action.run();
            return null;
        });
    }

    public static long stopWatch(Runnable action) {
        var then = System.currentTimeMillis();
        try {
            action.run();
        } finally {
            var now = System.currentTimeMillis();
            return now - then;
        }
    }

    static void printResults(Logger logger) {
        for (var entry : data.entrySet()) {
            logger.info(entry.getKey() + " " + entry.getValue() + "ms");
        }
    }

}
