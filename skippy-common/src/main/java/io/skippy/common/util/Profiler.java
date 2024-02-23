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

package io.skippy.common.util;

import io.skippy.common.SkippyConstants;
import io.skippy.common.SkippyFolder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static io.skippy.common.SkippyConstants.*;
import static java.lang.System.lineSeparator;
import static java.nio.file.StandardOpenOption.*;
import static java.util.stream.Collectors.joining;

/**
 * Simple DIY profiler that writes the {@link SkippyConstants#PROFILING_LOG_FILE} in the skippy folder every second.
 * <br /><br />
 * Note: Due to the complexity of build tools (e.g., Gradle might execute a build across multiple JVMs), this profiler
 * might or might not generate accurate or complete profiling data.
 *
 * @author Florian McKee
 */
public final class Profiler {

    private static final boolean PROFILING_ENABLED = false;

    record InvocationCountAndTime(AtomicInteger invocationCount, AtomicLong time) {};

    private static Map<String, InvocationCountAndTime> data = new ConcurrentHashMap<>();

    private static Instant lastSave = Instant.now();
    /**
     * Profiles the {@code supplier} under the given {@code label}.
     *
     * @param label a label
     * @param supplier a {@link Supplier}
     * @return the result from the {@code supplier}
     * @param <T> the {@code supplier}'s return type
     */
    public static <T> T profile(String label, Supplier<T> supplier) {
        if (lastSave.isBefore(Instant.now().minusSeconds(1))) {
            lastSave = Instant.now();
            writeResults(SkippyFolder.get());

        }
        if ( ! data.containsKey(label)) {
            data.put(label, new InvocationCountAndTime(new AtomicInteger(0), new AtomicLong(0L)));
        }
        long then = System.currentTimeMillis();
        var result = supplier.get();
        long now = System.currentTimeMillis();
        data.get(label).invocationCount.incrementAndGet();
        data.get(label).time.addAndGet(now - then);
        return result;
    }

    /**
     * Profiles the {@code runnable} under the given {@code label}.
     *
     * @param label a label
     * @param runnable a {@link Runnable}
     */
    public static void profile(String label, Runnable runnable) {
        profile(label, (Supplier<Void>) () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Writes the results to the profiling.log file in the skippy folder.
     *
     * @param skippyFolder the Skippy folder
     */
    public static void writeResults(Path skippyFolder) {
        if (PROFILING_ENABLED) {
            var result =  "=== %s ===%s%s%s%s".formatted(
                    Runtime.getRuntime().toString(),
                    System.lineSeparator(),
                    data.entrySet().stream()
                            .map(entry -> "%s: %s call(s), %sms".formatted(entry.getKey(), entry.getValue().invocationCount, entry.getValue().time))
                            .sorted()
                            .collect(joining(lineSeparator())),
                    System.lineSeparator(),
                    System.lineSeparator());
            try {
                Files.writeString(skippyFolder.resolve(PROFILING_LOG_FILE), result, StandardCharsets.UTF_8, CREATE, APPEND);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    public static void printResults() {
        var result =  "=== %s ===%s%s%s%s".formatted(
                Runtime.getRuntime().toString(),
                System.lineSeparator(),
                data.entrySet().stream()
                        .map(entry -> "%s: %s call(s), %sms".formatted(entry.getKey(), entry.getValue().invocationCount, entry.getValue().time))
                        .sorted()
                        .collect(joining(lineSeparator())),
                System.lineSeparator(),
                System.lineSeparator());
        System.out.println(result);
    }


    /**
     * Clears all profiling data.
     */
    public static void clear() {
        data.clear();
    }

}