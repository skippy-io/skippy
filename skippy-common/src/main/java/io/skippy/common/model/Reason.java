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

package io.skippy.common.model;

import java.util.Optional;

/**
 * The reason for a {@link Prediction}.
 */
public record Reason(Category category, Optional<String> details) {

    enum Category {
        /**
         * Neither the test nor any of the covered classes have changed.
         */
        NO_CHANGE,

        /**
         * Unknown test.
         */
        UNKNOWN_TEST,

        /**
         * Bytecode change in test detected.
         */
        BYTECODE_CHANGE_IN_TEST,

        /**
         * Bytecode change in covered class detected.
         */
        BYTECODE_CHANGE_IN_COVERED_CLASS,

        /**
         * The test failed previously.
         */
        TEST_FAILED_PREVIOUSLY,

        /**
         * The class file of the test was not found on the file system.
         */
        TEST_CLASS_CLASS_FILE_NOT_FOUND,

        /**
         * The class file of a covered class  was not found on the file system.
         */
        COVERED_CLASS_CLASS_FILE_NOT_FOUND
    }

}
