package io.skippy.junit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

class Profiler {

    private static final Logger LOGGER = LogManager.getLogger(Profiler.class);

    private static Map<String, Long> data = new HashMap<>();

    static <T> T profile(String id, Supplier<T> supplier) {
        if ( ! data.containsKey(id)) {
            data.put(id, 0L);
        }
        long then = System.currentTimeMillis();
        var result = supplier.get();
        long now = System.currentTimeMillis();
        data.put(id, data.get(id) + now - then);
        return result;
    }

    static void dump() {
        if (LOGGER.isDebugEnabled()) {
            for (var entry  : data.entrySet()) {
                LOGGER.debug("%s: %sms".formatted(entry.getKey(), entry.getValue()));
            }
        }
    }

}