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
import io.skippy.common.model.TestWithJacocoExecutionDataAndCoveredClasses;
import io.skippy.common.util.JacocoExecutionDataUtil;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.Arrays.asList;

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
    public final List<TestWithJacocoExecutionDataAndCoveredClasses> getTemporaryTestExecutionDataForCurrentBuild() {
        var result = new ArrayList<TestWithJacocoExecutionDataAndCoveredClasses>();
        List<Path> executionDataFiles = getTemporaryExecutionDataFilesForCurrentBuild();
        for (var executionDataFile : executionDataFiles) {
        var testName = executionDataFile.toFile().getName().substring(0, executionDataFile.toFile().getName().indexOf(".exec"));
            try {
                var bytes = Files.readAllBytes(executionDataFile);
                result.add(new TestWithJacocoExecutionDataAndCoveredClasses(testName, bytes, JacocoExecutionDataUtil.getCoveredClasses(bytes)));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return result;
    }

    @Override
    public final void saveTemporaryTestExecutionDataForCurrentBuild(String testClassName, byte[] executionData) {
        try {
            Files.write(SkippyFolder.get().resolve("%s.exec".formatted(testClassName)), executionData, CREATE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected final void deleteTemporaryExecutionDataFilesForCurrentBuild() {
        for (var executionDataFile : getTemporaryExecutionDataFilesForCurrentBuild()) {
            executionDataFile.toFile().delete();
        }
    }

    private List<Path> getTemporaryExecutionDataFilesForCurrentBuild() {
        var temporaryExecutionDataFiles = asList(SkippyFolder.get(projectDir).toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".exec")))
                .stream()
                .filter(file -> ! file.getName().matches("[A-Z0-9]{32}\\.exec"))
                .map(File::toPath).toList();
        return temporaryExecutionDataFiles;
    }

}