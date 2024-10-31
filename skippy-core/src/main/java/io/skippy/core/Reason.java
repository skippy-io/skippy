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

import java.util.Optional;

/**
 * The reason for a {@link Prediction}.
 *
 * @author Florian McKee
 */
record Reason(Category category, Optional<String> details) {

    enum Category {

        /**
         * Skippy was unable to retrieve an existing Test Impact Analysis to make a skip-or-execute decision.
         */
        TEST_IMPACT_ANALYSIS_NOT_FOUND,

        /**
         * Neither the test nor any of the covered classes have changed.
         */
        NO_CHANGE,

        /**
         * The test hasn't been analyzed before.
         */
        NO_DATA_FOUND_FOR_TEST,

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
         * A covered test failed previously. This is relevant for JUnit 5's @Nested tests.
         */
        COVERED_TEST_FAILED_PREVIOUSLY,

        /**
         * The class file of the test was not found on the file system.
         */
        TEST_CLASS_CLASS_FILE_NOT_FOUND,

        /**
         * The class file of a covered class  was not found on the file system.
         */
        COVERED_CLASS_CLASS_FILE_NOT_FOUND,

        /**
         * Coverage for skipped tests is enabled but the test has no execution id. The test needs to be re-run in order
         * to capture coverage for skipped tests.
         */
        MISSING_EXECUTION_ID,

        /**
         * Coverage for skipped tests is enabled and the test has an execution id. However, Skippy is unable to read the
         * execution data. The test needs to be re-run in order to capture coverage for skipped tests.
         */
        UNABLE_TO_READ_EXECUTION_DATA
    }

}
