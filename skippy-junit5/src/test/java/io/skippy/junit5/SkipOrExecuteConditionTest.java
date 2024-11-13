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

import io.skippy.core.RuntimeParameters;
import io.skippy.core.SkippyTestApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SkipOrExecuteCondition}.
 *
 * @author Florian McKee
 */
public class SkipOrExecuteConditionTest {

    @Test
    void testEmptyTestInstanceEqualsEnabled() {
        var skippyTestApi = mock(SkippyTestApi.class);
        var skippyExecutionCondition = new SkipOrExecuteCondition(skippyTestApi);
        ExtensionContext context = mock(ExtensionContext.class);

        when(context.getTestInstance()).thenReturn(Optional.empty());

        assertEquals(false, skippyExecutionCondition.evaluateExecutionCondition(context).isDisabled());
    }

    @Test
    void testSkippyAnalysisExecutionRequiredFalse() {
        var skippyTestApi = mock(SkippyTestApi.class);
        var skippyExecutionCondition = new SkipOrExecuteCondition(skippyTestApi);
        ExtensionContext context = mock(ExtensionContext.class);

        when(context.getTestInstance()).thenReturn(Optional.of(new Object()));
        when(context.getTestClass()).thenReturn(Optional.of(Object.class));
        when(skippyTestApi.testNeedsToBeExecuted(any(), any())).thenReturn(false);

        assertEquals(true, skippyExecutionCondition.evaluateExecutionCondition(context).isDisabled());
    }

    @Test
    void testSkippyAnalysisExecutionRequiredTrue() {
        var skippyTestApi = mock(SkippyTestApi.class);
        var skippyExecutionCondition = new SkipOrExecuteCondition(skippyTestApi);
        ExtensionContext context = mock(ExtensionContext.class);

        when(context.getTestInstance()).thenReturn(Optional.of(new Object()));
        when(context.getTestClass()).thenReturn(Optional.of(Object.class));
        when(skippyTestApi.testNeedsToBeExecuted(any(), any())).thenReturn(true);

        assertEquals(false, skippyExecutionCondition.evaluateExecutionCondition(context).isDisabled());
    }

}