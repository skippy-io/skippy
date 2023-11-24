package io.skippy.core.parser;

import io.skippy.core.model.SkippyAnalysisResult;
import io.skippy.core.SkippyConstants;

public class SkippyAnalysisResultParser {

    public static SkippyAnalysisResult parse() {
        if ( ! SkippyConstants.SKIPPY_DIRECTORY.toFile().exists() || ! SkippyConstants.SKIPPY_DIRECTORY.toFile().isDirectory()) {
            return SkippyAnalysisResult.UNAVAILABLE;
        }
        var sourceFileSnapshots = SourceFileSnapshotParser.parse(SkippyConstants.SKIPPY_DIRECTORY.resolve(SkippyConstants.SOURCE_SNAPSHOT_FILE));
        var testCoverage = TestImpactAnalysisParser.parse(SkippyConstants.SKIPPY_DIRECTORY);
        return new SkippyAnalysisResult(sourceFileSnapshots, testCoverage);
    }

}
