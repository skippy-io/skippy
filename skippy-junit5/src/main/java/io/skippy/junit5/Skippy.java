package io.skippy.junit5;

import io.skippy.core.parser.SkippyAnalysisResultParser;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class Skippy implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (context.getTestInstance().isEmpty()) {
            return ConditionEvaluationResult.enabled("");
        }
        var analysisResult = SkippyAnalysisResultParser.parse();
        if (analysisResult.executionRequired(context.getTestClass().get())) {
            return ConditionEvaluationResult.enabled("");
        }
        return ConditionEvaluationResult.disabled("");
    }


}
