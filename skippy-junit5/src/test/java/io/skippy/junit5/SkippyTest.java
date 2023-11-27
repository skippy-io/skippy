package io.skippy.junit5;

import io.skippy.core.SkippyAnalysis;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        when(skippyAnalysis.executionRequired(any())).thenReturn(false);

        assertEquals(true, skippy.evaluateExecutionCondition(context).isDisabled());
    }

    @Test
    void testSkippyAnalysisExecutionRequiredTrue() {
        var skippyAnalysis = mock(SkippyAnalysis.class);
        var skippy = new Skippy(skippyAnalysis);
        ExtensionContext context = mock(ExtensionContext.class);

        when(context.getTestInstance()).thenReturn(Optional.of(new Object()));
        when(context.getTestClass()).thenReturn(Optional.of(Object.class));
        when(skippyAnalysis.executionRequired(any())).thenReturn(true);

        assertEquals(false, skippy.evaluateExecutionCondition(context).isDisabled());
    }

}