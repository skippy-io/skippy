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

package io.skippy.build;


import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static io.skippy.core.SkippyConstants.CLASSES_MD5_FILE;
import static io.skippy.core.SkippyConstants.SKIPPY_DIRECTORY;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.writeString;
import static java.util.stream.Collectors.joining;

/**
 * Creates the classes.md5 file in the skippy folder.
 *
 * The file contains the following properties for all class files in the project:
 * <ul>
 *     <li>the classes directory</li>
 *     <li>the path of the class file in the classes directory</li>
 *     <li>a hash of the class file</li>
 * </ul>
 *
 * The properties are separated by a colon.
 * <br /><br />
 * Example:
 * <pre>
 * build/classes/java/intTest:com/example/StringUtilsTest.class:p+N8biKVOm6BltcZkKcC/g==
 * build/classes/java/main:com/example/StringUtils.class:4VP9fWGFUJHKIBG47OXZTQ==
 * ...
 * </pre>
 *
 * @author Florian McKee
 */
final class ClassesMd5Writer {

    private final Path projectDir;
    private final ClassFileCollector classFileCollector;

    ClassesMd5Writer(Path projectDir, ClassFileCollector classFileCollector) {
        this.projectDir = projectDir;
        this.classFileCollector = classFileCollector;
    }

    void write() {
        try {
            if ( ! SKIPPY_DIRECTORY.toFile().exists()) {
                SKIPPY_DIRECTORY.toFile().mkdirs();
            }
            var skippyAnalysisFile = projectDir.resolve(SKIPPY_DIRECTORY).resolve(CLASSES_MD5_FILE);
            skippyAnalysisFile.toFile().createNewFile();
            List<String> lines = new ArrayList<>();
            for (var directoryWithClasses : classFileCollector.collect()) {
                for (var classFile : directoryWithClasses.classFiles()) {
                    lines.add("%s:%s:%s".formatted(
                            projectDir.relativize(directoryWithClasses.directory()),
                            directoryWithClasses.directory().relativize(classFile.getAbsolutePath()),
                            classFile.getHash())
                    );
                }
            }
            writeString(skippyAnalysisFile, lines.stream().collect(joining(lineSeparator())));
        } catch (IOException e) {
            throw new UncheckedIOException("Writing of %s failed: %s".formatted(CLASSES_MD5_FILE, e.getMessage()), e);
        }
    }

}
