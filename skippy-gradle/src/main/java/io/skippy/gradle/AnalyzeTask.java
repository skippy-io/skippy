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

package io.skippy.gradle;

import io.skippy.gradle.collector.ClassFileCollector;
import io.skippy.gradle.model.ClassFile;
import org.gradle.api.DefaultTask;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.tasks.SourceSetContainer;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.skippy.gradle.SkippyConstants.CLASSES_MD5_FILE;
import static io.skippy.gradle.SkippyConstants.SKIPPY_DIRECTORY;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.writeString;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

/**
 * The tasks that is run via <code>./gradlew skippyAnalyze</code>.
 *
 * @author Florian McKee
 */
class AnalyzeTask extends DefaultTask {

    /**
     * C'tor.
     */
    @Inject
    public AnalyzeTask() {
        setGroup("skippy");
        for (var sourceSet : getProject().getExtensions().getByType(SourceSetContainer.class)) {
            dependsOn(sourceSet.getClassesTaskName());
        }
        dependsOn("skippyClean", "check");
        doLast((task) -> {
            var classFiles = collectAllClassFiles();
            createClassesMd5(classFiles);
            filterCoverageFiles(classFiles);
        });
    }

    private void createClassesMd5(Map<Path, List<ClassFile>> classFiles) {
        try {

            var skippyAnalysisFile = getProject().getProjectDir().toPath().resolve(SKIPPY_DIRECTORY).resolve(CLASSES_MD5_FILE);
            skippyAnalysisFile.toFile().createNewFile();
            getLogger().lifecycle("Storing hashes for all class files in %s.".formatted(getProject().getProjectDir().toPath().relativize(skippyAnalysisFile)));

            List<String> lines = new ArrayList<>();
            for (var pathWithClassFiles : classFiles.entrySet()) {
                for (var classFile : pathWithClassFiles.getValue()) {
                    var projectDir = getProject().getProjectDir().toPath();
                    lines.add("%s:%s:%s".formatted(
                            projectDir.relativize(pathWithClassFiles.getKey()),
                            pathWithClassFiles.getKey().relativize(classFile.getAbsolutePath()),
                            classFile.getHash())
                    );
                }
            }
            writeString(skippyAnalysisFile, lines.stream().collect(joining(lineSeparator())));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Removes all entries from .cov files that do not have corresponding class file in one of the output folders of the
     * project.
     *
     * The purpose is to remove classes from libraries.
     *
     * <pre>
     * org/gradle/internal/serialize/BaseSerializerFactory$BooleanSerializer
     * org/apache/logging/log4j/spi/GarbageFreeSortedArrayThreadContextMap
     * org/junit/jupiter/engine/descriptor/LifecycleMethodUtils
     * ...
     * </pre>
     */
    private void filterCoverageFiles(Map<Path, List<ClassFile>> classFilesByDirectory) {
        try {
            Set<String> classFiles = classFilesByDirectory.values().stream().flatMap(it -> it.stream().map(ClassFile::getFullyQualifiedClassName)).collect(toSet());
            for (var covFile : getProject().getProjectDir().toPath().resolve(SKIPPY_DIRECTORY).toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".cov"))) {
                List<String> filteredLines = new ArrayList<>();
                for (String clazz : Files.readAllLines(covFile.toPath(), StandardCharsets.UTF_8)) {
                    if (classFiles.contains(clazz.replace("/", "."))) {
                        filteredLines.add(clazz.replace("/", "."));
                    }
                }
                getLogger().lifecycle("Storing coverage data in %s.".formatted(getProject().getProjectDir().toPath().relativize(covFile.toPath())));
                Files.write(covFile.toPath(), filteredLines.stream().sorted().toList(), StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    private Map<Path, List<ClassFile>> collectAllClassFiles() {
        var sourceSetContainer = getProject().getExtensions().getByType(SourceSetContainer.class);
        var classFileCollector = new ClassFileCollector(getProject(), sourceSetContainer);
        return classFileCollector.collect();
    }

}