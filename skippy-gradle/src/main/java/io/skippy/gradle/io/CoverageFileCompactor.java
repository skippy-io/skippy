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

package io.skippy.gradle.io;

import io.skippy.gradle.collector.ClassFileCollector;
import io.skippy.gradle.model.ClassFile;
import org.gradle.api.logging.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static io.skippy.gradle.Constants.SKIPPY_DIRECTORY;

/**
 * Removes all entries from .cov files in the skippy folder that do not have corresponding class file in one of the
 * output folders of the project. The main purpose is to make it easy for the human eye to inspect .cov files. A nice
 * side effect is the reduction in file size.
 *
 * <br /><br />
 *
 * Example: .cov file before compaction:
 *
 * <pre>
 * org/gradle/internal/serialize/BaseSerializerFactory$BooleanSerializer
 * com/example/Foo
 * org/apache/logging/log4j/spi/GarbageFreeSortedArrayThreadContextMap
 * com/example/FooTest
 * org/junit/jupiter/engine/descriptor/LifecycleMethodUtils*
 * </pre>
 *
 * Example: .cov file after compaction:
 *
 * <pre>
 * com/example/Foo
 * com/example/FooTest
 * </pre>
 *
 * @author Florian McKee
 */
public final class CoverageFileCompactor {

    private final ClassFileCollector classFileCollector;

    /**
     * C'tor.
     *
     * @param classFileCollector a {@link ClassFileCollector}
     */
    public CoverageFileCompactor(ClassFileCollector classFileCollector) {
        this.classFileCollector = classFileCollector;
    }

    /**
     * Compacts all coverage files in the skippy folder.
     *
     * @param logger the {@link Logger}
     * @param projectDir the project root
     */
    public void compact(Logger logger, Path projectDir) {
        try {
            List<String> fullyQualifiedClassNames = classFileCollector.collect().stream()
                    .flatMap(dir -> dir.classFiles().stream())
                    .map(ClassFile::getFullyQualifiedClassName)
                    .toList();
            for (var covFile : projectDir.resolve(SKIPPY_DIRECTORY).toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".cov"))) {
                List<String> filteredLines = new ArrayList<>();
                for (String clazz : Files.readAllLines(covFile.toPath(), StandardCharsets.UTF_8)) {
                    if (fullyQualifiedClassNames.contains(clazz.replace("/", "."))) {
                        filteredLines.add(clazz.replace("/", "."));
                    }
                }
                logger.lifecycle("Writing %s".formatted(projectDir.relativize(SKIPPY_DIRECTORY.resolve(covFile.toPath()))));
                Files.write(covFile.toPath(), filteredLines.stream().sorted().toList(), StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

}
