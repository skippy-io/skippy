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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link Skippy}.
 *
 * @author Florian McKee
 */
public class SkippyTest {

    @Test
    void testEmptyTestInstanceEqualsEnabled() {
        var skippyAnalysis = mock(SkippyAnalysis.class);
        var skippy = new Skippy(skippyAnalysis);
        ExtensionContext context = mock(ExtensionContext.class);

        when(context.getTestInstance()).thenReturn(Optional.empty());

        assertEquals(false, skippy.evaluateExecutionCondition(context).isDisabled());
    }

    @Test
    void testSkippyAnalysisExecutionRequiredFalse() {
        var skippyAnalysis = mock(SkippyAnalysis.class);
        var skippy = new Skippy(skippyAnalysis);
        ExtensionContext context = mock(ExtensionContext.class);

        when(context.getTestInstance()).thenReturn(Optional.of(new Object()));
        when(context.getTestClass()).thenReturn(Optional.of(Object.class));
        when(skippyAnalysis.execute(any())).thenReturn(false);

        assertEquals(true, skippy.evaluateExecutionCondition(context).isDisabled());
    }

    @Test
    void testSkippyAnalysisExecutionRequiredTrue() {
        var skippyAnalysis = mock(SkippyAnalysis.class);
        var skippy = new Skippy(skippyAnalysis);
        ExtensionContext context = mock(ExtensionContext.class);

        when(context.getTestInstance()).thenReturn(Optional.of(new Object()));
        when(context.getTestClass()).thenReturn(Optional.of(Object.class));
        when(skippyAnalysis.execute(any())).thenReturn(true);

        assertEquals(false, skippy.evaluateExecutionCondition(context).isDisabled());
    }

}