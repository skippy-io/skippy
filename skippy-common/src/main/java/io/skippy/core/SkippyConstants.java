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

import java.nio.file.Path;

/**
 * Comment to make the JavaDoc task happy.
 *
 * @author Florian McKee
 */
public class SkippyConstants {

    /**
     * Directory that contains the Skippy analysis.
     */
    public static final Path SKIPPY_DIRECTORY = Path.of("skippy");

    /**
     * File with hashes of all class files.
     */
    public static final Path CLASSES_MD5_FILE = Path.of("classes.md5");

    /**
     * Log file for skip-or-execute predictions.
     */
    public static final Path PREDICTIONS_LOG_FILE = Path.of("predictions.log");

    /**
     * Log file for profiling data.
     */
    public static final Path PROFILING_LOG_FILE = Path.of("profiling.log");

    /**
     * Environment variable that is set when a Test Impact Analysis is running. It instructs Skippy's JUnit libraries
     * (e.g., skippy-junit5) to emit coverage data for skippified tests.
     */
    public static final String TEST_IMPACT_ANALYSIS_RUNNING_MARKER = "skippyAnalyze";

}