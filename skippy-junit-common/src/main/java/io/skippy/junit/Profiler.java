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

package io.skippy.junit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Simple DIY profiler.
 *
 * @author Florian McKee
 */
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