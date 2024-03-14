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

package io.skippy.common.repository;

import io.skippy.common.SkippyFolder;
import io.skippy.common.model.SkippyConfiguration;
import io.skippy.common.model.TestWithJacocoExecutionDataAndCoveredClasses;
import io.skippy.common.util.JacocoExecutionDataUtil;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.*;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * Implementation for {@link SkippyRepository} methods that deal with storage of retrieval of temporary files that are
 * used for communication between Skippy's JUnit and build libraries.
 *
 * @author Florian McKee
 */
public abstract class AbstractSkippyRepository implements SkippyRepository {

    protected final Path projectDir;

    protected AbstractSkippyRepository(Path projectDir) {
        this.projectDir = projectDir;
    }


    @Override
    public void deleteSkippyFolder() {
        try {
            var skippyFolder = SkippyFolder.get(projectDir);
            if (exists(skippyFolder)) {
                try (var stream = newDirectoryStream(skippyFolder)) {
                    for (Path file : stream) {
                        delete(file);
                    }
                }
                delete(skippyFolder);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to delete the Skippy folder: %s.".formatted(e), e);
        }
    }

    @Override
    public final void saveConfiguration(SkippyConfiguration skippyConfiguration) {
        try {
            Files.writeString(SkippyFolder.get(projectDir).resolve("config.json"), skippyConfiguration.toJson(), StandardCharsets.UTF_8, CREATE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to save Skippy configuration: %s.".formatted(e.getMessage()), e);
        }
    }

    @Override
    public void deleteLogFiles() {
        try (var directoryStream  = Files.newDirectoryStream(SkippyFolder.get(projectDir),
                file -> file.getFileName().toString().endsWith(".log"))) {
            for (var logFile : directoryStream) {
                delete(logFile);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to delete log files: %s.".formatted(e.getMessage()), e);
        }
    }

    @Override
    public final List<TestWithJacocoExecutionDataAndCoveredClasses> getTemporaryTestExecutionDataForCurrentBuild() {
        var result = new ArrayList<TestWithJacocoExecutionDataAndCoveredClasses>();
        for (var executionDataFile : getTemporaryExecutionDataFilesForCurrentBuild()) {
            var filename = executionDataFile.getFileName().toString();
            var testName = filename.substring(0, filename.indexOf(".exec"));
            try {
                var bytes = Files.readAllBytes(executionDataFile);
                result.add(new TestWithJacocoExecutionDataAndCoveredClasses(testName, bytes, JacocoExecutionDataUtil.getCoveredClasses(bytes)));
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to get temporary test execution data for current build: %s.".formatted(e.getMessage()), e);
            }
        }
        return result;
    }

    @Override
    public final void saveTemporaryTestExecutionDataForCurrentBuild(String testClassName, byte[] executionData) {
        try {
            Files.write(SkippyFolder.get(projectDir).resolve("%s.exec".formatted(testClassName)), executionData, CREATE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to save temporary test execution data file for current build: %s / %s.".formatted(testClassName, e.getMessage()), e);
        }
    }

    protected final void deleteTemporaryExecutionDataFilesForCurrentBuild() {
        for (var executionDataFile : getTemporaryExecutionDataFilesForCurrentBuild()) {
            try {
                delete(executionDataFile);
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to delete %s.".formatted(executionDataFile), e);
            }
        }
    }

    private List<Path> getTemporaryExecutionDataFilesForCurrentBuild() {
        try {
            var result = new ArrayList<Path>();
            try (var directoryStream  = Files.newDirectoryStream(SkippyFolder.get(projectDir),
                    file -> file.getFileName().toString().endsWith(".exec") && false == file.getFileName().toString().matches("[A-Z0-9]{32}\\.exec"))) {
                for (var executionDataFile : directoryStream) {
                    result.add(executionDataFile);
                }
            }
            return result;
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to retrieve temporary execution data files for current build: %s".formatted(e), e);
        }
    }

}