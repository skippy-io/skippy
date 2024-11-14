package io.skippy.core;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefaultPredictionModifierTest {

    class NormalTestClass {
    }

    @AlwaysRun
    class AnnotatedTestClass {
    }

    @Test
    void testEvaluationOfNormalClass() {
        var modifier = new DefaultPredictionModifier();
        var actual = modifier.passThruOrModify(NormalTestClass.class, PredictionWithReason.skip(new Reason(Reason.Category.NO_CHANGE, Optional.empty())));
        assertEquals(Prediction.SKIP, actual.prediction());
    }

    @Test
    void testEvaluationOfClassAnnotatedWithAlwaysRun() {
        var modifier = new DefaultPredictionModifier();
        var actual = modifier.passThruOrModify(AnnotatedTestClass.class, PredictionWithReason.skip(new Reason(Reason.Category.NO_CHANGE, Optional.empty())));
        assertEquals(Prediction.ALWAYS_EXECUTE, actual.prediction());
    }
}
