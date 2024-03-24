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

package io.skippy.core;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JacocoExecutionDataUtilTest {

    @Test
    void testGetExecutionId() throws URISyntaxException, IOException {
        var leftPadderTestExecutionDataFile = Path.of(getClass().getResource("com.example.LeftPadderTest.exec").toURI());
        assertEquals("F94F1606CFCA75C46D4E2CECF86DD5C4", HashUtil.hashWith32Digits(Files.readAllBytes(leftPadderTestExecutionDataFile)));
        assertEquals("D40016DC6B856D89EA17DB14F370D026", JacocoUtil.getExecutionId(Files.readAllBytes(leftPadderTestExecutionDataFile)));

        // getExecutionId yields the same id if the only difference between two execution data instances is the session info block
        var leftPadderTestExecutionDataFileForExecution2 = Path.of(getClass().getResource("com.example.LeftPadderTest-run2.exec").toURI());
        assertEquals("ACE148F18B1D3DCC623160C6CF0849A4", HashUtil.hashWith32Digits(Files.readAllBytes(leftPadderTestExecutionDataFileForExecution2)));
        assertEquals("D40016DC6B856D89EA17DB14F370D026", JacocoUtil.getExecutionId(Files.readAllBytes(leftPadderTestExecutionDataFileForExecution2)));

        var rightPadderTestExecutionDataFile = Path.of(getClass().getResource("com.example.RightPadderTest.exec").toURI());
        assertEquals("8AF444DB651C3930E724886027566607", JacocoUtil.getExecutionId(Files.readAllBytes(rightPadderTestExecutionDataFile)));
    }
    @Test

    void testGetCoveredClasses() throws URISyntaxException, IOException {
        var leftPadderTestExecutionDataFile = Path.of(getClass().getResource("com.example.LeftPadderTest.exec").toURI());
        var coveredClasses = JacocoUtil.getCoveredClasses(Files.readAllBytes(leftPadderTestExecutionDataFile)).stream()
                .filter(clazz -> clazz.startsWith("com.example"))
                .toList();
        assertEquals(asList("com.example.LeftPadder", "com.example.LeftPadderTest", "com.example.StringUtils"), coveredClasses);


        var rightPadderTestExecutionDataFile = Path.of(getClass().getResource("com.example.RightPadderTest.exec").toURI());
        coveredClasses = JacocoUtil.getCoveredClasses(Files.readAllBytes(rightPadderTestExecutionDataFile)).stream()
                .filter(clazz -> clazz.startsWith("com.example"))
                .toList();
        assertEquals(asList("com.example.RightPadder", "com.example.RightPadderTest", "com.example.StringUtils"), coveredClasses);
    }

}
