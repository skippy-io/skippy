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
 * interfaces it implements is annotated with @{@link DisableSkippy}.
 *
 * @author Florian McKee
 */
final class DefaultPredictionModifier implements PredictionModifier {

    public DefaultPredictionModifier() {
    }

    @Override
    public PredictionWithReason passThruOrModify(Class<?> test, PredictionWithReason prediction) {
        if (isAnnotatedWithDisableSkippy(test)) {
            return PredictionWithReason.execute(new Reason(
                    Reason.Category.OVERRIDE_BY_PREDICTION_MODIFIER,
                    Optional.of("%s is annotated with %s".formatted(test.getName(), DisableSkippy.class.getSimpleName()))
            ));
        }
        return prediction;
    }

    private static boolean isAnnotatedWithDisableSkippy(Class<?> clazz) {
        if (clazz.isAnnotationPresent(DisableSkippy.class)) {
            return true;
        }
        for (var iface : clazz.getInterfaces()) {
            if (isAnnotatedWithDisableSkippy(iface)) {
                return true;
            }
        }
        if (clazz.getSuperclass() != null) {
            return isAnnotatedWithDisableSkippy(clazz.getSuperclass());
        }
        return false;
    }

}