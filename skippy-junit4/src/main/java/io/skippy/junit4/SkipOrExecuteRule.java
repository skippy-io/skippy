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

package io.skippy.junit4;

import io.skippy.core.SkippyTestApi;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * {@link TestRule} that makes skip-or-execute predictions for test.
 *
 * @author Florian McKee
 */
class SkipOrExecuteRule implements TestRule {

    private final SkippyTestApi skippyTestApi;

    public SkipOrExecuteRule() {
        this(SkippyTestApi.INSTANCE);
    }

    SkipOrExecuteRule(SkippyTestApi skippyTestApi) {
        this.skippyTestApi = skippyTestApi;
    }

    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (executeTest(description.getTestClass())) {
                    base.evaluate();
                } else {
                    throw new AssumptionViolatedException("Test skipped by Skippy.");
                }
            }

            private boolean executeTest(Class<?> testClass) {
                return skippyTestApi.testNeedsToBeExecuted(testClass);
            }
        };
    }
}
