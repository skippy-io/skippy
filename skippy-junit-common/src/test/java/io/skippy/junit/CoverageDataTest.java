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

package io.skippy.junit;

import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link CoverageData}.
 *
 * @author Florian McKee
 */
public class CoverageDataTest {

    @Test
    void testParse() throws URISyntaxException {
        var directory = Path.of(getClass().getResource("coveragedata").toURI());
        var tia = CoverageData.parse(directory);

        asList(false, tia.noDataAvailableFor(new FullyQualifiedClassName("com.example.LeftPadderTest")));

        assertEquals(asList(
                new FullyQualifiedClassName("com.example.StringUtils"),
                new FullyQualifiedClassName("com.example.LeftPadder"),
                new FullyQualifiedClassName("com.example.LeftPadderTest")
        ), tia.getCoveredClasses(new FullyQualifiedClassName("com.example.LeftPadderTest")));

        asList(false, tia.noDataAvailableFor(new FullyQualifiedClassName("com.example.RightPadderTest")));

        assertEquals(asList(
                new FullyQualifiedClassName("com.example.StringUtils"),
                new FullyQualifiedClassName("com.example.RightPadder"),
                new FullyQualifiedClassName("com.example.RightPadderTest")
        ), tia.getCoveredClasses(new FullyQualifiedClassName("com.example.RightPadderTest")));

        asList(true, tia.noDataAvailableFor(new FullyQualifiedClassName("com.example.UnknownClass")));
    }

}