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

package io.skippy.junit5;

import io.skippy.junit.SkippyTestApi;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstancePreConstructCallback;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;

/**
 * Callbacks that notify the {@link SkippyTestApi} before and after the execution of a test.
 * The {@link SkippyTestApi} uses those notifications to generate a coverage file for the test.
 *
 * @author Florian McKee
 */
final class CoverageFileCallbacks implements TestInstancePreConstructCallback, TestInstancePreDestroyCallback {

    @Override
    public void preConstructTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext context) {
        context.getTestClass().ifPresent(SkippyTestApi::beforeTestClass);
    }

    @Override
    public void preDestroyTestInstance(ExtensionContext context) {
        context.getTestClass().ifPresent(SkippyTestApi::afterTestClass);
    }

}