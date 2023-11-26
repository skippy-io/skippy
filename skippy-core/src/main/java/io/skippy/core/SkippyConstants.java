package io.skippy.core;

import java.nio.file.Path;

/**
 * Comment to make the JavaDoc task happy.
 */
class SkippyConstants {

    /**
     * The directory that contains the Skippy analysis.
     */
    static final Path SKIPPY_DIRECTORY = Path.of("skippy");

    /**
     * The file that contains data for all {@link AnalyzedFile}s.
     */
    static final Path SKIPPY_ANALYSIS_FILE = Path.of("analyzedFiles.txt");

}