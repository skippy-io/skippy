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
 * A list of {@link AnalyzedClass}s with a couple of utility methods that operates on this list.
 *
 * @author Florian McKee
 */
class AnalyzedClassList {

    private static final Logger LOGGER = LogManager.getLogger(AnalyzedClassList.class);

    static final AnalyzedClassList UNAVAILABLE = new AnalyzedClassList(emptyList());

    private final List<AnalyzedClass> analyzedClasses;

    /**
     * C'tor.
     *
     * @param analyzedClasses a list of {@link AnalyzedClass}s
     */
    private AnalyzedClassList(List<AnalyzedClass> analyzedClasses) {
        this.analyzedClasses = analyzedClasses;
    }

    static AnalyzedClassList parse(Path skippyAnalysisFile) {
        if (!skippyAnalysisFile.toFile().exists()) {
            return UNAVAILABLE;
        }
        try {
            var result = new ArrayList<AnalyzedClass>();
            for (var line : Files.readAllLines(skippyAnalysisFile, Charset.forName("UTF8"))) {
                String[] split = line.split(":");
                result.add(new AnalyzedClass(Path.of(split[0]), split[1]));
            }
            return new AnalyzedClassList(result);
        } catch (Exception e) {
            LOGGER.error("Parsing of file '%s' failed: '%s'".formatted(skippyAnalysisFile, e.getMessage()), e);
            throw new RuntimeException(e);
        }
    }

    List<FullyQualifiedClassName> getClasses() {
        return analyzedClasses.stream()
                .map(s -> s.getFullyQualifiedClassName())
                .toList();
    }

    List<FullyQualifiedClassName> getClassesWithBytecodeChanges() {
        return analyzedClasses.stream()
                .filter(s -> s.hasChanged())
                .map(s -> s.getFullyQualifiedClassName())
                .toList();
    }

}
