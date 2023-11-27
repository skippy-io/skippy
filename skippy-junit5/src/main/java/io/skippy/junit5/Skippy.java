package io.skippy.junit5;

import io.skippy.core.SkippyAnalysis;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Decides whether a test case needs to run based on Skippy analysis.
 */
public class Skippy implements ExecutionCondition {

    private final SkippyAnalysis skippyAnalysis;

    public Skippy() {
        this(SkippyAnalysis.parse());
    }

    Skippy(final SkippyAnalysis skippyAnalysis) {
        this.skippyAnalysis = skippyAnalysis;
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (context.getTestInstance().isEmpty()) {
            return ConditionEvaluationResult.enabled("");
        }
        if (skippyAnalysis.executionRequired(context.getTestClass().get())) {
            return ConditionEvaluationResult.enabled("");
        }
        return ConditionEvaluationResult.disabled("");
    }


}
