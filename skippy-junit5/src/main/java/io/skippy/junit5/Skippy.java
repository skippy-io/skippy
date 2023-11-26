package io.skippy.junit5;

import io.skippy.core.SkippyAnalysis;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Decides whether a test case needs to run based on Skippy analysis.
 */
public class Skippy implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (context.getTestInstance().isEmpty()) {
            return ConditionEvaluationResult.enabled("");
        }
        var analysisResult = SkippyAnalysis.parse();
        if (analysisResult.executionRequired(context.getTestClass().get())) {
            return ConditionEvaluationResult.enabled("");
        }
        return ConditionEvaluationResult.disabled("");
    }


}
