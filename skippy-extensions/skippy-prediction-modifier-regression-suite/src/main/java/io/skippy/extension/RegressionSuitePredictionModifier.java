package io.skippy.extension;

import io.skippy.core.*;

import java.util.Optional;

/**
 * Custom {@link PredictionModifier} that is internally used by the tests in skippy-regression-suite.
 */
public class RegressionSuitePredictionModifier implements PredictionModifier  {

    @Override
    public PredictionWithReason passThruOrModify(Class<?> test, ParametersFromBuildPlugin parametersFromBuildPlugin, PredictionWithReason prediction) {
        return new PredictionWithReason(Prediction.ALWAYS_EXECUTE, new Reason(Reason.Category.OVERRIDE_BY_PREDICTION_MODIFIER, Optional.of("RegressionSuitePredictionModifier")));
    }
}
