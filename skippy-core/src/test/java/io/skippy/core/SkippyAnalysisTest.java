package io.skippy.core;

import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SkippyAnalysisTest {

    @Test
    void smokeTestParse() throws URISyntaxException {
        var skippyFolder = Path.of(getClass().getResource("skippyanalysis").toURI());
        var skippyAnalysis = SkippyAnalysis.parse(skippyFolder);
        assertEquals(true, skippyAnalysis.executionRequired(String.class));
    }

    @Test
    void noCoverageDataEqualsExecution() {
        var analyzedFiles = mock(AnalyzedFileList.class);
        var testImpactAnalysis = mock(TestImpactAnalysis.class);
        var skippyAnalysis = new SkippyAnalysis(analyzedFiles, testImpactAnalysis);

        when(testImpactAnalysis.noDataAvailableFor(new FullyQualifiedClassName("io.skippy.core.SkippyAnalysisTest"))).thenReturn(true);

        assertEquals(true, skippyAnalysis.executionRequired(SkippyAnalysisTest.class));
    }

    @Test
    void testWithSourceChangeEqualsExecution() {
        var analyzedFiles = mock(AnalyzedFileList.class);
        var testImpactAnalysis = mock(TestImpactAnalysis.class);
        var skippyAnalysis = new SkippyAnalysis(analyzedFiles, testImpactAnalysis);
        var skippyAnalysisTest = new FullyQualifiedClassName("io.skippy.core.SkippyAnalysisTest");

        when(testImpactAnalysis.noDataAvailableFor(skippyAnalysisTest)).thenReturn(false);
        when(analyzedFiles.getClassesWithSourceChanges()).thenReturn(asList(skippyAnalysisTest));

        assertEquals(true, skippyAnalysis.executionRequired(SkippyAnalysisTest.class));
    }

    @Test
    void testWithBytecodeChangeEqualsExecution() {
        var analyzedFiles = mock(AnalyzedFileList.class);
        var testImpactAnalysis = mock(TestImpactAnalysis.class);
        var skippyAnalysis = new SkippyAnalysis(analyzedFiles, testImpactAnalysis);
        var skippyAnalysisTest = new FullyQualifiedClassName("io.skippy.core.SkippyAnalysisTest");

        when(testImpactAnalysis.noDataAvailableFor(skippyAnalysisTest)).thenReturn(false);
        when(analyzedFiles.getClassesWithSourceChanges()).thenReturn(emptyList());
        when(analyzedFiles.getClassesWithBytecodeChanges()).thenReturn(asList(skippyAnalysisTest));

        assertEquals(true, skippyAnalysis.executionRequired(SkippyAnalysisTest.class));
    }

    @Test
    void testCoveredClassWithSourceChangeEqualsExecution() {
        var analyzedFiles = mock(AnalyzedFileList.class);
        var testImpactAnalysis = mock(TestImpactAnalysis.class);
        var skippyAnalysis = new SkippyAnalysis(analyzedFiles, testImpactAnalysis);
        var skippyAnalysisTest = new FullyQualifiedClassName("io.skippy.core.SkippyAnalysisTest");
        var foo = new FullyQualifiedClassName("com.example.Foo");

        when(testImpactAnalysis.noDataAvailableFor(skippyAnalysisTest)).thenReturn(false);
        when(testImpactAnalysis.getCoveredClasses(skippyAnalysisTest)).thenReturn(asList(foo));
        when(analyzedFiles.getClassesWithSourceChanges()).thenReturn(asList(foo));
        when(analyzedFiles.getClassesWithBytecodeChanges()).thenReturn(emptyList());

        assertEquals(true, skippyAnalysis.executionRequired(SkippyAnalysisTest.class));
    }

    @Test
    void testCoveredClassWithBytecodeChangeEqualsExecution() {
        var analyzedFiles = mock(AnalyzedFileList.class);
        var testImpactAnalysis = mock(TestImpactAnalysis.class);
        var skippyAnalysis = new SkippyAnalysis(analyzedFiles, testImpactAnalysis);
        var skippyAnalysisTest = new FullyQualifiedClassName("io.skippy.core.SkippyAnalysisTest");
        var foo = new FullyQualifiedClassName("com.example.Foo");

        when(testImpactAnalysis.noDataAvailableFor(skippyAnalysisTest)).thenReturn(false);
        when(testImpactAnalysis.getCoveredClasses(skippyAnalysisTest)).thenReturn(asList(foo));
        when(analyzedFiles.getClassesWithSourceChanges()).thenReturn(emptyList());
        when(analyzedFiles.getClassesWithBytecodeChanges()).thenReturn(asList(foo));

        assertEquals(true, skippyAnalysis.executionRequired(SkippyAnalysisTest.class));
    }

    @Test
    void testSkipIfNothingHasChanged() {
        var analyzedFiles = mock(AnalyzedFileList.class);
        var testImpactAnalysis = mock(TestImpactAnalysis.class);
        var skippyAnalysis = new SkippyAnalysis(analyzedFiles, testImpactAnalysis);
        var skippyAnalysisTest = new FullyQualifiedClassName("io.skippy.core.SkippyAnalysisTest");
        var foo = new FullyQualifiedClassName("com.example.Foo");

        when(testImpactAnalysis.noDataAvailableFor(skippyAnalysisTest)).thenReturn(false);
        when(testImpactAnalysis.getCoveredClasses(skippyAnalysisTest)).thenReturn(asList(foo));
        when(analyzedFiles.getClassesWithSourceChanges()).thenReturn(emptyList());
        when(analyzedFiles.getClassesWithBytecodeChanges()).thenReturn(emptyList());

        assertEquals(false, skippyAnalysis.executionRequired(SkippyAnalysisTest.class));
    }

}