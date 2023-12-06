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

package io.skippy.gradle.asm;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.URISyntaxException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link SkippyJUnit5Detector}.
 *
 * @author Florian McKee
 */
public class SkippyJunit5DetectorTest {

    @ParameterizedTest
    @CsvSource(value = {
            "LeftPadderTest.class:true",
            "SkippyConstants.class:false",
            "SkippyPlugin.class:false"
    }, delimiter = ':')
    void testUsesSkippyExtension(String fileName, boolean expected) throws URISyntaxException {
        var classFile = Paths.get(getClass().getResource(fileName).toURI());
        assertEquals(expected, SkippyJUnit5Detector.usesSkippyJunit5Extension(classFile));
    }

}