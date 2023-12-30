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

package io.skippy.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static io.skippy.core.SkippyConstants.PROFILING_DATA_FILE;
import static io.skippy.core.SkippyConstants.SKIPPY_DIRECTORY;
import static java.lang.System.lineSeparator;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.util.stream.Collectors.joining;

/**
 * Simple DIY profiler.
 *
 * @author Florian McKee
 */
public class Profiler {

    private static Map<String, Long> data = new HashMap<>();

    static {
        Thread printingHook = new Thread(() -> {
            try {

                Files.writeString(SKIPPY_DIRECTORY.resolve(PROFILING_DATA_FILE), Profiler.getResults(), StandardCharsets.UTF_8, CREATE, APPEND);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        Runtime.getRuntime().addShutdownHook(printingHook);
    }

    /**
     * Profiles the {@code supplier} under the given {@code label}.
     *
     * @param label a label
     * @param supplier a {@link Supplier}
     * @return the result from the {@code supplier}
     * @param <T> the {@code supplier}'s return type
     */
    public static <T> T profile(String label, Supplier<T> supplier) {
        if ( ! data.containsKey(label)) {
            data.put(label, 0L);
        }
        long then = System.currentTimeMillis();
        var result = supplier.get();
        long now = System.currentTimeMillis();
        data.put(label, data.get(label) + now - then);
        return result;
    }


    /**
     * Returns the profiling results.
     *
     * @return the profiling results
     */
    public static String getResults() {
        return "=== %s ===%s%s%s%s".formatted(
                Runtime.getRuntime().toString(),
                System.lineSeparator(),
                data.entrySet().stream().map(entry -> "%s: %sms".formatted(entry.getKey(), entry.getValue())).collect(joining(lineSeparator())),
                System.lineSeparator(),
                System.lineSeparator());
    }

}