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

import io.skippy.junit.SkippyTestApi;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * {@link ExecutionCondition} that makes skip-or-execute predictions for tests.
 *
 * @author Florian McKee
 */
public final class SkipOrExecuteCondition implements ExecutionCondition {

    private final SkippyTestApi skippyTestApi;

    /**
     * Comment to make the JavaDoc task happy.
     */
    public SkipOrExecuteCondition() {
        this(SkippyTestApi.INSTANCE);
    }

    SkipOrExecuteCondition(final SkippyTestApi skippyTestApi) {
        this.skippyTestApi = skippyTestApi;
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (context.getTestClass().isEmpty()) {
            return ConditionEvaluationResult.enabled("");
        }
        if (skippyTestApi.testNeedsToBeExecuted(context.getTestClass().get())) {
            return ConditionEvaluationResult.enabled("");
        }
        return ConditionEvaluationResult.disabled("");
    }

}