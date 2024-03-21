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

package io.skippy.core;

import java.nio.file.Path;

/**
 * Comment to make the JavaDoc task happy.
 *
 * @author Florian McKee
 */
final class SkippyConstants {

    /**
     * Directory that contains the Skippy analysis.
     */
    static final Path SKIPPY_DIRECTORY = Path.of(".skippy");

    /**
     * Log file for skip-or-execute predictions.
     */
    static final Path PREDICTIONS_LOG_FILE = Path.of("predictions.log");

    /**
     * Log file for profiling data.
     */
    static final Path PROFILING_LOG_FILE = Path.of("profiling.log");

}