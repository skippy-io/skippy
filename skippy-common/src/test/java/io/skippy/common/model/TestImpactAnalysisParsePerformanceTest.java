package io.skippy.common.model;

import io.skippy.common.util.Profiler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestImpactAnalysisParsePerformanceTest {

    @Test
    void testParse() throws URISyntaxException, IOException {
        var jsonFile = Paths.get(getClass().getResource("test-impact-analysis.json").toURI());
        TestImpactAnalysis.parse(Files.readString(jsonFile, StandardCharsets.UTF_8));
        Profiler.printResults();
    }

}