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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link SkippyAnalysis}.
 *
 * @author Florian McKee
 */
public class SkippyAnalysisTest {

    @Test
    void test_no_change_equals_skip() throws URISyntaxException {
        var skippyFolder = Path.of(getClass().getResource("skippyanalysis/test1/skippy").toURI());
        var skippyAnalysis = SkippyAnalysis.parse(skippyFolder);

        var decisonWithReason = skippyAnalysis.execute(new FullyQualifiedClassName("com.example.LeftPadderTest"));

        assertEquals(false, decisonWithReason.execute());
        assertEquals(SkippyAnalysis.Reason.NO_CHANGE, decisonWithReason.reason());
    }

    @Test
    void test_no_coverage_data_for_test_equals_execute() throws URISyntaxException {
        var skippyFolder = Path.of(getClass().getResource("skippyanalysis/test2/skippy").toURI());
        var skippyAnalysis = SkippyAnalysis.parse(skippyFolder);

        var decisonWithReason = skippyAnalysis.execute(new FullyQualifiedClassName("com.example.LeftPadderTest"));

        assertEquals(true, decisonWithReason.execute());
        assertEquals(SkippyAnalysis.Reason.NO_COVERAGE_DATA_FOR_TEST, decisonWithReason.reason());
    }

    @Test
    void test_bytecode_change_in_test_equals_execute() throws URISyntaxException {
        var skippyFolder = Path.of(getClass().getResource("skippyanalysis/test3/skippy").toURI());
        var skippyAnalysis = SkippyAnalysis.parse(skippyFolder);

        var decisonWithReason = skippyAnalysis.execute(new FullyQualifiedClassName("com.example.LeftPadderTest"));

        assertEquals(true, decisonWithReason.execute());
        assertEquals(SkippyAnalysis.Reason.BYTECODE_CHANGE_IN_TEST, decisonWithReason.reason());
    }

    @Test
    void test_bytecode_change_in_covered_class_equals_execute() throws URISyntaxException {
        var skippyFolder = Path.of(getClass().getResource("skippyanalysis/test4/skippy").toURI());
        var skippyAnalysis = SkippyAnalysis.parse(skippyFolder);

        var decisonWithReason = skippyAnalysis.execute(new FullyQualifiedClassName("com.example.LeftPadderTest"));

        assertEquals(true, decisonWithReason.execute());
        assertEquals(SkippyAnalysis.Reason.BYTECODE_CHANGE_IN_COVERED_CLASS, decisonWithReason.reason());
    }

    @Test
    void test_missing_hash_for_test_equals_execute() throws URISyntaxException {
        var skippyFolder = Path.of(getClass().getResource("skippyanalysis/test5/skippy").toURI());
        var skippyAnalysis = SkippyAnalysis.parse(skippyFolder);

        var decisonWithReason = skippyAnalysis.execute(new FullyQualifiedClassName("com.example.LeftPadderTest"));

        assertEquals(true, decisonWithReason.execute());
        assertEquals(SkippyAnalysis.Reason.NO_HASH_FOR_TEST, decisonWithReason.reason());
    }

    @Test
    void test_missing_hash_for_covered_class_equals_execute() throws URISyntaxException {
        var skippyFolder = Path.of(getClass().getResource("skippyanalysis/test6/skippy").toURI());
        var skippyAnalysis = SkippyAnalysis.parse(skippyFolder);

        var decisonWithReason = skippyAnalysis.execute(new FullyQualifiedClassName("com.example.LeftPadderTest"));

        assertEquals(true, decisonWithReason.execute());
        assertEquals(SkippyAnalysis.Reason.NO_HASH_FOR_COVERED_CLASS, decisonWithReason.reason());
    }

}