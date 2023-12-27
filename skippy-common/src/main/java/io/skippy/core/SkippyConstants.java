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
     * The directory that contains the Skippy analysis.
     */
    public static final Path SKIPPY_DIRECTORY = Path.of("skippy");

    /**
     * The classes.md5 file that contains hashes for all class files.
     */
    public static final Path CLASSES_MD5_FILE = Path.of("classes.md5");

    /**
     * Environment variable that is set when the Skippy analysis is executed.
     */
    public static final String SKIPPY_ANALYZE_ENVIRONMENT_VARIABLE = "skippyAnalyze";

}