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

import io.skippy.core.SkippyAnalysis;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * {@link ExecutionCondition} that decides whether to run or skip a test based a {@link SkippyAnalysis}.
 *
 * @author Florian McKee
 */
final class SkipOrExecuteCondition implements ExecutionCondition {

    private final SkippyAnalysis skippyAnalysis;

    /**
     * Comment to make the JavaDoc task happy.
     */
    public SkipOrExecuteCondition() {
        this(SkippyAnalysis.getInstance());
    }

    SkipOrExecuteCondition(final SkippyAnalysis skippyAnalysis) {
        this.skippyAnalysis = skippyAnalysis;
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (context.getTestInstance().isEmpty()) {
            return ConditionEvaluationResult.enabled("");
        }
        if (skippyAnalysis.testNeedsToBeExecuted(context.getTestClass().get())) {
            return ConditionEvaluationResult.enabled("");
        }
        return ConditionEvaluationResult.disabled("");
    }

}