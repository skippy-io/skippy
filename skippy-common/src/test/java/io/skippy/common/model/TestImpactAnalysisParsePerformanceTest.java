package io.skippy.common.model;

import io.skippy.common.SkippyConstants;
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
        var jsonFile = Paths.get(getClass().getResource(SkippyConstants.TEST_IMPACT_ANALYSIS_JSON_FILE.toString()).toURI());
        TestImpactAnalysis.parse(Files.readString(jsonFile, StandardCharsets.UTF_8));
        Profiler.printResults();
    }

}