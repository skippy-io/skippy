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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import static io.skippy.core.SkippyConstants.SKIPPY_DIRECTORY;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;

/**
 * A couple of static methods for retrieval and creation of the skippy folder.
 *
 * @author Florian McKee
 */
final class SkippyFolder {

    /**
     * Returns the Skippy folder. This method will create the folder if it doesn't exist.
     *
     * @return the Skippy folder
     */
    static Path get() {
        return get(Path.of("."));
    }

    /**
     * Returns the Skippy folder in the given {@code projectFolder}. This method will create the folder if it doesn't
     * exist.
     *
     * @param projectFolder the project's root folder
     * @return the Skippy folder in the given {@code projectFolder}
     */
    static Path get(Path projectFolder) {
        try {
            var skippyFolder = projectFolder.resolve(SKIPPY_DIRECTORY);
            if (false == exists(skippyFolder)) {
                createDirectories(skippyFolder);
            }
            return skippyFolder;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create Skippy folder: %s".formatted(e.getMessage()), e);
        }
    }

}