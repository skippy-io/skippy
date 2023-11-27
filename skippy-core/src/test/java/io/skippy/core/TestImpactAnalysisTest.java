package io.skippy.core;

import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestImpactAnalysisTest {

    @Test
    void testParse() throws URISyntaxException {
        var directory = Path.of(getClass().getResource("testimpactanalysis").toURI());
        var tia = TestImpactAnalysis.parse(directory);

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