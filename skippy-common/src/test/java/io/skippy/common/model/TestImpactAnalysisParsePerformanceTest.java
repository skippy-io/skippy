package io.skippy.common.model;

import io.skippy.common.SkippyConstants;
import io.skippy.common.util.Profiler;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestImpactAnalysisParsePerformanceTest {

    @Disabled("json file needs to be re-created with latest")
    @Test
    void testParse() throws URISyntaxException {

        var jsonFile = Paths.get(getClass().getResource(SkippyConstants.TEST_IMPACT_ANALYSIS_JSON_FILE.toString()).toURI());

        var result = TestImpactAnalysis.readFromFile(jsonFile);
        Profiler.printResults();
    }



}
