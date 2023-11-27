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

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * A list of {@link AnalyzedFile}s with a couple of utility methods that operates on this list.
 *
 * @author Florian McKee
 */
class AnalyzedFileList {

    private static final Logger LOGGER = LogManager.getLogger(AnalyzedFileList.class);

    static final AnalyzedFileList UNAVAILABLE = new AnalyzedFileList(emptyList());

    private final List<AnalyzedFile> analyzedFiles;

    /**
     * C'tor.
     *
     * @param analyzedFiles a list of {@link AnalyzedFile}s
     */
    private AnalyzedFileList(List<AnalyzedFile> analyzedFiles) {
        this.analyzedFiles = analyzedFiles;
    }

    static AnalyzedFileList parse(Path skippyAnalysisFile) {
        if (!skippyAnalysisFile.toFile().exists()) {
            return UNAVAILABLE;
        }
        try {
            var result = new ArrayList<AnalyzedFile>();
            for (var line : Files.readAllLines(skippyAnalysisFile, Charset.forName("UTF8"))) {
                String[] split = line.split(":");
                result.add(new AnalyzedFile(new FullyQualifiedClassName(split[0]), Path.of(split[1]), Path.of(split[2]), split[3], split[4]));
            }
            return new AnalyzedFileList(result);
        } catch (Exception e) {
            LOGGER.error("Parsing of file '%s' failed: '%s'".formatted(skippyAnalysisFile, e.getMessage()), e);
            throw new RuntimeException(e);
        }
    }

    List<FullyQualifiedClassName> getClasses() {
        return analyzedFiles.stream()
                .map(s -> s.getFullyQualifiedClassName())
                .toList();
    }

    List<FullyQualifiedClassName> getClassesWithSourceChanges() {
        return analyzedFiles.stream()
                .filter(s -> s.sourceFileHasChanged())
                .map(s -> s.getFullyQualifiedClassName())
                .toList();
    }

    List<FullyQualifiedClassName> getClassesWithBytecodeChanges() {
        return analyzedFiles.stream()
                .filter(s -> s.classFileHasChanged())
                .map(s -> s.getFullyQualifiedClassName())
                .toList();
    }

}
