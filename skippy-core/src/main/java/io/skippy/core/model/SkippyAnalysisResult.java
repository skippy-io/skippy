package io.skippy.core.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static java.util.Collections.emptyList;

/**
 * The result of a Skippy Analysis:
 * <ul>
 *     <li>{@link SourceFileSnapshot}s for all sources</li>
 *     <li>a {@link TestImpactAnalysis} for all tests that use the Skippy extension</li>
 * </ul>
 */
public class SkippyAnalysisResult {

    private static final Logger LOGGER = LogManager.getLogger(SkippyAnalysisResult.class);

    public static final SkippyAnalysisResult UNAVAILABLE = new SkippyAnalysisResult(emptyList(), TestImpactAnalysis.UNAVAILABLE);

    private final List<SourceFileSnapshot> sourceFileSnapshots;
    private final TestImpactAnalysis testImpactAnalysis;

    public SkippyAnalysisResult(List<SourceFileSnapshot> sourceFileSnapshots, TestImpactAnalysis testImpactAnalysis) {
        this.sourceFileSnapshots = sourceFileSnapshots;
        this.testImpactAnalysis = testImpactAnalysis;
    }

    public boolean executionRequired(Class<?> testClass) {
        var test = new FullyQualifiedClassName(testClass.getName());
        if (testImpactAnalysis.noDataAvailableFor(test)) {
            LOGGER.debug("%s: No analysis found. Execution required.".formatted(test.fullyQualifiedClassName()));
            return true;
        }
        if (getClassesWithSourceChanges().contains(test)) {
            LOGGER.debug("%s: Source change detected. Execution required.".formatted(
                    test.fullyQualifiedClassName()
            ));
            return true;
        }
        if (getClassesWithBytecodeChanges().contains(test)) {
            LOGGER.debug("%s: Bytecode change detected. Execution required.".formatted(
                    test.fullyQualifiedClassName()
            ));
            return true;
        }
        if (coveredClassHasChanged(test)) {
            return true;
        }
        LOGGER.debug("%s: No changes in test or covered classes detected. Execution skipped.".formatted(
                test.fullyQualifiedClassName()
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
        return sourceFileSnapshots.stream()
                .filter(s -> s.sourceFileHasChanged())
                .map(s -> s.getFullyQualifiedClassName())
                .toList();
    }

    private List<FullyQualifiedClassName> getClassesWithBytecodeChanges() {
        return sourceFileSnapshots.stream()
                .filter(s -> s.classFileHasChanged())
                .map(s -> s.getFullyQualifiedClassName())
                .toList();
    }

}