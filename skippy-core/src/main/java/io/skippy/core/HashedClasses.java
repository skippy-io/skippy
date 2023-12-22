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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * In-memory representation of the {@code classes.md5} file.
 *
 * @author Florian McKee
 */
class HashedClasses {

    private static final Logger LOGGER = LogManager.getLogger(HashedClasses.class);

    static final HashedClasses UNAVAILABLE = new HashedClasses(emptyList());

    private final List<HashedClass> hashedClasses;

    /**
     * C'tor.
     *
     * @param hashedClasses a list of {@link HashedClass}s
     */
    private HashedClasses(List<HashedClass> hashedClasses) {
        this.hashedClasses = hashedClasses;
    }

    static HashedClasses parse(Path classesMd5File) {
        return Profiler.profile("HashedClasses#parse", () -> {
            if (!classesMd5File.toFile().exists()) {
                return UNAVAILABLE;
            }
            try {
                var result = new ArrayList<HashedClass>();
                for (var line : Files.readAllLines(classesMd5File, StandardCharsets.UTF_8)) {
                    String[] split = line.split(":");
                    result.add(new HashedClass(Path.of("%s/%s".formatted(split[0], split[1])), split[2]));
                }
                return new HashedClasses(result);
            } catch (Exception e) {
                LOGGER.error("Parsing of file '%s' failed: '%s'".formatted(classesMd5File, e.getMessage()), e);
                throw new RuntimeException(e);
            }
        });
    }

    List<FullyQualifiedClassName> getClasses() {
        return Profiler.profile("HashedClasses#getClasses", () -> {
            return hashedClasses.stream()
                    .map(s -> s.getFullyQualifiedClassName())
                    .toList();
        });
    }

    List<FullyQualifiedClassName> getChangedClasses() {
        return Profiler.profile("HashedClasses#getChangedClasses", () -> {
            return hashedClasses.stream()
                    .filter(hashedClass -> hashedClass.exists())
                    .filter(hashedClass -> hashedClass.hasChanged())
                    .map(hashedClass -> hashedClass.getFullyQualifiedClassName())
                    .toList();
        });
    }

    boolean noDataFor(FullyQualifiedClassName fqn) {
        return Profiler.profile("HashedClasses#noDataFor", () -> {
            return !hashedClasses.stream()
                    .filter(hashedClass -> hashedClass.exists())
                    .anyMatch(hashedClass -> fqn.equals(hashedClass.getFullyQualifiedClassName()));
        });
    }
}
