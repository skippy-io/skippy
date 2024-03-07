package io.skippy.common.model;

import io.skippy.common.SkippyConstants;
import io.skippy.common.util.Profiler;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Paths;

public class TestImpactAnalysisParsePerformanceTest {

    @Test
    void testParse() throws URISyntaxException {
        var jsonFile = Paths.get(getClass().getResource(SkippyConstants.TEST_IMPACT_ANALYSIS_JSON_FILE.toString()).toURI());
        TestImpactAnalysis.readFromFile(jsonFile);
        Profiler.printResults();
    }

}