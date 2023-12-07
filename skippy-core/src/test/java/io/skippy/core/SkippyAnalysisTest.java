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

package io.skippy.core;

import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SkippyAnalysis}.
 *
 * @author Florian McKee
 */
public class SkippyAnalysisTest {

    @Test
    void smokeTestParse() throws URISyntaxException {
        var skippyFolder = Path.of(getClass().getResource("skippyanalysis").toURI());
        var skippyAnalysis = SkippyAnalysis.parse(skippyFolder);
        assertEquals(true, skippyAnalysis.executionRequired(String.class));
    }

    @Test
    void noCoverageDataEqualsExecution() {
        var analyzedFiles = mock(ClassFileList.class);
        var testImpactAnalysis = mock(TestImpactAnalysis.class);
        var skippyAnalysis = new SkippyAnalysis(analyzedFiles, testImpactAnalysis);

        when(testImpactAnalysis.noDataAvailableFor(new FullyQualifiedClassName("io.skippy.core.SkippyAnalysisTest"))).thenReturn(true);

        assertEquals(true, skippyAnalysis.executionRequired(SkippyAnalysisTest.class));
    }

    @Test
    void testWithBytecodeChangeEqualsExecution() {
        var analyzedFiles = mock(ClassFileList.class);
        var testImpactAnalysis = mock(TestImpactAnalysis.class);
        var skippyAnalysis = new SkippyAnalysis(analyzedFiles, testImpactAnalysis);
        var skippyAnalysisTest = new FullyQualifiedClassName("io.skippy.core.SkippyAnalysisTest");

        when(testImpactAnalysis.noDataAvailableFor(skippyAnalysisTest)).thenReturn(false);
        when(analyzedFiles.getChangedClasses()).thenReturn(asList(skippyAnalysisTest));

        assertEquals(true, skippyAnalysis.executionRequired(SkippyAnalysisTest.class));
    }

    @Test
    void testCoveredClassWithBytecodeChangeEqualsExecution() {
        var analyzedFiles = mock(ClassFileList.class);
        var testImpactAnalysis = mock(TestImpactAnalysis.class);
        var skippyAnalysis = new SkippyAnalysis(analyzedFiles, testImpactAnalysis);
        var skippyAnalysisTest = new FullyQualifiedClassName("io.skippy.core.SkippyAnalysisTest");
        var foo = new FullyQualifiedClassName("com.example.Foo");

        when(testImpactAnalysis.noDataAvailableFor(skippyAnalysisTest)).thenReturn(false);
        when(testImpactAnalysis.getCoveredClasses(skippyAnalysisTest)).thenReturn(asList(foo));
        when(analyzedFiles.getChangedClasses()).thenReturn(asList(foo));

        assertEquals(true, skippyAnalysis.executionRequired(SkippyAnalysisTest.class));
    }

    @Test
    void testSkipIfNothingHasChanged() {
        var analyzedFiles = mock(ClassFileList.class);
        var testImpactAnalysis = mock(TestImpactAnalysis.class);
        var skippyAnalysis = new SkippyAnalysis(analyzedFiles, testImpactAnalysis);
        var skippyAnalysisTest = new FullyQualifiedClassName("io.skippy.core.SkippyAnalysisTest");
        var foo = new FullyQualifiedClassName("com.example.Foo");

        when(testImpactAnalysis.noDataAvailableFor(skippyAnalysisTest)).thenReturn(false);
        when(testImpactAnalysis.getCoveredClasses(skippyAnalysisTest)).thenReturn(asList(foo));
        when(analyzedFiles.getChangedClasses()).thenReturn(emptyList());

        assertEquals(false, skippyAnalysis.executionRequired(SkippyAnalysisTest.class));
    }

}