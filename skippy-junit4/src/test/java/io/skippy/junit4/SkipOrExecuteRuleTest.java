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
import io.skippy.junit.SkippyTestApi;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static org.mockito.Mockito.*;

/**
 * Tests for {@link SkipOrExecuteRule}
 */
public class SkipOrExecuteRuleTest {

    SkippyTestApi skippyTestApi = mock(SkippyTestApi.class);
    Statement base = mock(Statement.class);
    Description description = mock(Description.class);

    @Test
    public void testExecutionofTest() throws Throwable {
        doReturn(SkipOrExecuteRuleTest.class).when(description).getTestClass();
        var rule = new SkipOrExecuteRule(skippyTestApi);
        when(skippyTestApi.testNeedsToBeExecuted(SkipOrExecuteRuleTest.class)).thenReturn(true);
        rule.apply(base, description).evaluate();
        verify(base).evaluate();
    }

    @Test
    public void testSkippingOfTest() throws Throwable {
        doReturn(SkipOrExecuteRuleTest.class).when(description).getTestClass();
        var rule = new SkipOrExecuteRule(skippyTestApi);
        when(skippyTestApi.testNeedsToBeExecuted(SkipOrExecuteRuleTest.class)).thenReturn(false);
        rule.apply(base, description).evaluate();
        verify(base, times(0)).evaluate();
    }

}