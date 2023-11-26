package io.skippy.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static java.util.Collections.emptyList;

/**
 * The result of a Skippy analysis:
 * <ul>
 *     <li>a list of {@link AnalyzedFile}s</li>
 *     <li>a {@link TestImpactAnalysis} for all tests that use the Skippy extension</li>
 * </ul>
 */
public class SkippyAnalysis {

    private static final Logger LOGGER = LogManager.getLogger(SkippyAnalysis.class);

    private static final SkippyAnalysis UNAVAILABLE = new SkippyAnalysis(emptyList(), TestImpactAnalysis.UNAVAILABLE);

    private final List<AnalyzedFile> analyzedFiles;
    private final TestImpactAnalysis testImpactAnalysis;

    /**
     * C'tor.
     *
     * @param analyzedFiles all files that have been analyzed by Skippy's analysis
     * @param testImpactAnalysis the {@link TestImpactAnalysis} created by Skippy's analysis
     */
    private SkippyAnalysis(List<AnalyzedFile> analyzedFiles, TestImpactAnalysis testImpactAnalysis) {
        this.analyzedFiles = analyzedFiles;
        this.testImpactAnalysis = testImpactAnalysis;
    }

    /**
     * Parses the content of the skippy folder to generate a {@link SkippyAnalysis}.
     *
     * @return a {@link SkippyAnalysis}
     */
    public static SkippyAnalysis parse() {
        if ( ! SkippyConstants.SKIPPY_DIRECTORY.toFile().exists() || ! SkippyConstants.SKIPPY_DIRECTORY.toFile().isDirectory()) {
            return SkippyAnalysis.UNAVAILABLE;
        }
        var sourceFileSnapshots = AnalyzedFile.parse(SkippyConstants.SKIPPY_DIRECTORY.resolve(SkippyConstants.SKIPPY_ANALYSIS_FILE));
        var testCoverage = TestImpactAnalysis.parse(SkippyConstants.SKIPPY_DIRECTORY);
        return new SkippyAnalysis(sourceFileSnapshots, testCoverage);
    }

    /**
     * Returns {@code true} if {@param test} needs to be executed, {@code false} otherwise.
     *
     * @param test a class object representing a test
     * @return {@code true} if {@param test} needs to be executed, {@code false} otherwise
     */
    public boolean executionRequired(Class<?> test) {
        var testFqn = new FullyQualifiedClassName(test.getName());
        if (testImpactAnalysis.noDataAvailableFor(testFqn)) {
            LOGGER.debug("%s: No analysis found. Execution required.".formatted(testFqn.fullyQualifiedClassName()));
            return true;
        }
        if (getClassesWithSourceChanges().contains(testFqn)) {
            LOGGER.debug("%s: Source change detected. Execution required.".formatted(
                    testFqn.fullyQualifiedClassName()
            ));
            return true;
        }
        if (getClassesWithBytecodeChanges().contains(testFqn)) {
            LOGGER.debug("%s: Bytecode change detected. Execution required.".formatted(
                    testFqn.fullyQualifiedClassName()
            ));
            return true;
        }
        if (coveredClassHasChanged(testFqn)) {
            return true;
        }
        LOGGER.debug("%s: No changes in test or covered classes detected. Execution skipped.".formatted(
                testFqn.fullyQualifiedClassName()
        ));
        return false;
    }

    private boolean coveredClassHasChanged(FullyQualifiedClassName test) {
        var changedClassesWithSourceChanges = getClassesWithSourceChanges();
        for (var coveredClass : testImpactAnalysis.getCoveredClasses(test)) {
            if (changedClassesWithSourceChanges.contains(coveredClass)) {
                LOGGER.debug("%s: Source change in covered class '%s' detected. Execution required.".formatted(
                        test.fullyQualifiedClassName(),
                        coveredClass.fullyQualifiedClassName()
                ));
                return true;
            }
        }
        var changedClassesWithBytecodeChanges = getClassesWithBytecodeChanges();
        for (var coveredClass : testImpactAnalysis.getCoveredClasses(test)) {
            if (changedClassesWithBytecodeChanges.contains(coveredClass)) {
                LOGGER.debug("%s: Bytecode change in covered class '%s' detected. Execution required.".formatted(
                        test.fullyQualifiedClassName(),
                        coveredClass.fullyQualifiedClassName()
                ));
                return true;
            }
        }
        return false;
    }

    private List<FullyQualifiedClassName> getClassesWithSourceChanges() {
        return analyzedFiles.stream()
                .filter(s -> s.sourceFileHasChanged())
                .map(s -> s.getFullyQualifiedClassName())
                .toList();
    }

    private List<FullyQualifiedClassName> getClassesWithBytecodeChanges() {
        return analyzedFiles.stream()
                .filter(s -> s.classFileHasChanged())
                .map(s -> s.getFullyQualifiedClassName())
                .toList();
    }

}