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

import java.nio.file.Path;

import static io.skippy.core.SkippyConstants.*;

/**
 * API with functionality that is used across build-tool specific libraries (e.g., skippy-gradle and skippy-maven).
 *
 * @author Florian McKee
 */
public final class SkippyBuildApi {

    private final Path projectDir;
    private final ClassesMd5Writer classesMd5Writer;
    private final CoverageFileCompactor coverageFileCompactor;

    /**
     * C'tor.
     *
     * @param projectDir the project folder
     * @param buildLogger the {@link BuildLogger}
     * @param classFileCollector the {@link ClassFileCollector}
     */
    public SkippyBuildApi(Path projectDir, BuildLogger buildLogger, ClassFileCollector classFileCollector) {
        this.projectDir = projectDir;
        this.classesMd5Writer = new ClassesMd5Writer(projectDir, buildLogger, classFileCollector);
        this.coverageFileCompactor = new CoverageFileCompactor(projectDir, buildLogger, classFileCollector);
    }

    /**
     * Performs the following actions:
     * <ul>
     *     <li>Compacts the coverage files (see {@link CoverageFileCompactor})</li>
     *     <li>Writes the {@code classes.md5} file (see {@link ClassesMd5Writer})</li>
     * </ul>
     */
    public void writeClassesMd5FileAndCompactCoverageFiles() {
        classesMd5Writer.write();
        coverageFileCompactor.compact();
    }

    /**
     * Clears the skippy directory.
     */
    public void clearSkippyFolder() {
        if (! projectDir.resolve(SKIPPY_DIRECTORY).toFile().exists()) {
            projectDir.resolve(SKIPPY_DIRECTORY).toFile().mkdir();
        }
        for (var file : projectDir.resolve(SKIPPY_DIRECTORY).toFile().listFiles()) {
            file.delete();
        }
    }

    /**
     * Creates the skippy folder if it doesn't exist and remove decision.log and profiling.log files from the skippy
     * folder if they exist.
     */
    public void deleteLogFilesAndCreateSkippyFolderIfItDoesntExist() {
        var skippyFolder = projectDir.resolve(SKIPPY_DIRECTORY);
        skippyFolder.toFile().mkdirs();

        var decisionLog = skippyFolder.resolve(DECISION_LOG_FILE).toFile();
        if (decisionLog.exists()) {
            decisionLog.delete();
        }
        var profilingLog = skippyFolder.resolve(PROFILING_LOG_FILE).toFile();
        if (profilingLog.exists()) {
            profilingLog.delete();
        }
    }

}
