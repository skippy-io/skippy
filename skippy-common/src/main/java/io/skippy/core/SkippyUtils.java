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

import static io.skippy.core.SkippyConstants.SKIPPY_DIRECTORY;

/**
 * A couple of static methods for retrieval and creation of the skippy folder.
 *
 * @author Florian McKee
 */
public final class SkippyUtils {

    /**
     * Returns the skippy folder. This method will create the folder if it doesn't exist.
     *
     * @return the skippy folder
     */
    public static Path getOrCreateSkippyFolder() {
        return getOrCreateSkippyFolder(Path.of("."));
    }

    /**
     * Returns the skippy folder in the given {@code projectFolder}. This method will create the folder if it doesn't
     * exist.
     *
     * @param projectFolder the project's root folder
     * @return the skippy folder in the given {@code projectFolder}
     */
    public static Path getOrCreateSkippyFolder(Path projectFolder) {
        var skippyFolder = getSkippyFolder(projectFolder);
        if ( ! skippyFolder.toFile().exists()) {
            skippyFolder.toFile().mkdirs();
        }
        return skippyFolder;
    }

    /**
     * Returns the skippy folder in the given {@code projectFolder}.
     *
     * @param projectFolder the project's root folder
     * @return the skippy folder in the given {@code projectFolder}
     */
    public static Path getSkippyFolder(Path projectFolder) {
        return projectFolder.resolve(SKIPPY_DIRECTORY);
    }

    /**
     * Returns the skippy folder.
     *
     * @return the skippy folder
     */
    public static Path getSkippyFolder() {
        return getSkippyFolder(Path.of("."));
    }

}