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
 * {@link PredictionModifier} that defaults to {@link Prediction#EXECUTE} if a test (or one of it's superclasses or
 * interfaces it implements is annotated with @{@link AlwaysRun}.
 *
 * @author Florian McKee
 */
final class DefaultPredictionModifier implements PredictionModifier {

    public DefaultPredictionModifier() {
    }

    @Override
    public PredictionWithReason passThruOrModify(Class<?> test, ParametersFromBuildPlugin parametersFromBuildPlugin, PredictionWithReason prediction) {
        if (isAnnotatedWithAlwaysRun(test)) {
            return new PredictionWithReason(
                Prediction.ALWAYS_EXECUTE,
                new Reason(
                    Reason.Category.OVERRIDE_BY_PREDICTION_MODIFIER,
                    Optional.of("Class, superclass or implementing interface annotated with @%s".formatted(AlwaysRun.class.getSimpleName()))
                )
            );
        }
        return prediction;
    }

    private static boolean isAnnotatedWithAlwaysRun(Class<?> clazz) {
        if (clazz.isAnnotationPresent(AlwaysRun.class)) {
            return true;
        }
        for (var interfce : clazz.getInterfaces()) {
            if (isAnnotatedWithAlwaysRun(interfce)) {
                return true;
            }
        }
        if (clazz.getSuperclass() != null) {
            return isAnnotatedWithAlwaysRun(clazz.getSuperclass());
        }
        return false;
    }

}