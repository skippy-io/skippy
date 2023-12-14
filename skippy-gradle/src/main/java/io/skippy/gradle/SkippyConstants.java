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

import io.skippy.gradle.model.ClassFile;

import java.nio.file.Path;

/**
 * Comment to make the JavaDoc task happy.
 *
 * @author Florian McKee
 */
public final class SkippyConstants {

    /**
     * The directory that contains the Skippy analysis.
     */
    public static final Path SKIPPY_DIRECTORY = Path.of("skippy");

    /**
     * The file that contains data for all {@link ClassFile}s.
     */
    public static final Path CLASSES_MD5_FILE = Path.of("classes.md5");

    
}