package io.skippy.common.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JacocoExecutionDataUtilTest {

    @Test
    void testGetExecutionId() throws URISyntaxException, IOException {
        var leftPadderTestExecutionDataFile = Path.of(getClass().getResource("com.example.LeftPadderTest.exec").toURI());
        assertEquals("F94F1606CFCA75C46D4E2CECF86DD5C4", HashUtil.hashWith32Digits(Files.readAllBytes(leftPadderTestExecutionDataFile)));
        assertEquals("D40016DC6B856D89EA17DB14F370D026", JacocoExecutionDataUtil.getExecutionId(Files.readAllBytes(leftPadderTestExecutionDataFile)));

        // getExecutionId yields the same id if the only difference between two execution data instances is the session info block
        var leftPadderTestExecutionDataFileForExecution2 = Path.of(getClass().getResource("com.example.LeftPadderTest-run2.exec").toURI());
        assertEquals("ACE148F18B1D3DCC623160C6CF0849A4", HashUtil.hashWith32Digits(Files.readAllBytes(leftPadderTestExecutionDataFileForExecution2)));
        assertEquals("D40016DC6B856D89EA17DB14F370D026", JacocoExecutionDataUtil.getExecutionId(Files.readAllBytes(leftPadderTestExecutionDataFileForExecution2)));

        var rightPadderTestExecutionDataFile = Path.of(getClass().getResource("com.example.RightPadderTest.exec").toURI());
        assertEquals("8AF444DB651C3930E724886027566607", JacocoExecutionDataUtil.getExecutionId(Files.readAllBytes(rightPadderTestExecutionDataFile)));
    }
    @Test

    void testGetCoveredClasses() throws URISyntaxException, IOException {
        var leftPadderTestExecutionDataFile = Path.of(getClass().getResource("com.example.LeftPadderTest.exec").toURI());
        var coveredClasses = JacocoExecutionDataUtil.getCoveredClasses(Files.readAllBytes(leftPadderTestExecutionDataFile)).stream()
                .filter(clazz -> clazz.startsWith("com.example"))
                .toList();
        assertEquals(asList("com.example.LeftPadder", "com.example.StringUtils", "com.example.LeftPadderTest"), coveredClasses);


        var rightPadderTestExecutionDataFile = Path.of(getClass().getResource("com.example.RightPadderTest.exec").toURI());
        coveredClasses = JacocoExecutionDataUtil.getCoveredClasses(Files.readAllBytes(rightPadderTestExecutionDataFile)).stream()
                .filter(clazz -> clazz.startsWith("com.example"))
                .toList();
        assertEquals(asList("com.example.StringUtils", "com.example.RightPadderTest", "com.example.RightPadder"), coveredClasses);
    }

}
