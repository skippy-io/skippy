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

package io.skippy.junit5;

import io.skippy.core.SkippyTestApi;
import io.skippy.core.TestTag;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

/**
 * Callbacks that trigger the capture of coverage data for a test class.
 *
 * @author Florian McKee
 */
public final class TestResultExtension implements TestWatcher {

    private final SkippyTestApi skippyTestApi = SkippyTestApi.INSTANCE;

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        skippyTestApi.tagTest(context.getTestClass().get(), TestTag.FAILED);
    }

}