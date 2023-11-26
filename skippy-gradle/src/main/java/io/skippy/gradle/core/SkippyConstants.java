package io.skippy.gradle.core;

import java.nio.file.Path;

/**
 * Comment to make the JavaDoc task happy.
 */
public class SkippyConstants {

    /**
     * The directory that contains the Skippy analysis.
     */
    public static final Path SKIPPY_DIRECTORY = Path.of("skippy");

    /**
     * The file that contains data for all {@link AnalyzedFile}s.
     */
    public static final Path SKIPPY_ANALYSIS_FILE = Path.of("analyzedFiles.txt");

    
}