/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.skippy.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

/**
 * The result of a Skippy analysis:
 * <ul>
 *     <li>a {@link AnalyzedClass} list</li>
 *     <li>a {@link TestImpactAnalysis} for all tests that use the Skippy extension</li>
 * </ul>
 *
 * @author Florian McKee
 */
public class SkippyAnalysis {

    private static final Logger LOGGER = LogManager.getLogger(SkippyAnalysis.class);

    private static final SkippyAnalysis UNAVAILABLE = new SkippyAnalysis(AnalyzedClassList.UNAVAILABLE, TestImpactAnalysis.UNAVAILABLE);

    private final AnalyzedClassList analyzedClasses;
    private final TestImpactAnalysis testImpactAnalysis;

    /**
     * C'tor.
     *
     * @param analyzedClasses all classes that have been analyzed by Skippy's analysis
     * @param testImpactAnalysis the {@link TestImpactAnalysis} created by Skippy's analysis
     */
    SkippyAnalysis(AnalyzedClassList analyzedClasses, TestImpactAnalysis testImpactAnalysis) {
        this.analyzedClasses = analyzedClasses;
        this.testImpactAnalysis = testImpactAnalysis;
    }

    /**
     * Parses the content of the skippy folder to generate a {@link SkippyAnalysis}.
     *
     * @return a {@link SkippyAnalysis}
     */
    public static SkippyAnalysis parse() {
        return parse(SkippyConstants.SKIPPY_DIRECTORY);
    }

    static SkippyAnalysis parse(Path skippyDirectory) {
        if ( ! skippyDirectory.toFile().exists() || ! skippyDirectory.toFile().isDirectory()) {
            return SkippyAnalysis.UNAVAILABLE;
        }
        var analyzedFiles = AnalyzedClassList.parse(skippyDirectory.resolve(SkippyConstants.SKIPPY_ANALYSIS_FILE));
        var testCoverage = TestImpactAnalysis.parse(skippyDirectory);
        return new SkippyAnalysis(analyzedFiles, testCoverage);
    }

    /**
     * Returns {@code true} if {@code test} needs to be executed, {@code false} otherwise.
     *
     * @param test a class object representing a test
     * @return {@code true} if {@code test} needs to be executed, {@code false} otherwise
     */
    public boolean executionRequired(Class<?> test) {
        var testFqn = new FullyQualifiedClassName(test.getName());
        if (testImpactAnalysis.noDataAvailableFor(testFqn)) {
            LOGGER.debug("%s: No analysis found. Execution required.".formatted(testFqn.fqn()));
            return true;
        }
        if (analyzedClasses.getChangedClasses().contains(testFqn)) {
            LOGGER.debug("%s: Bytecode change detected. Execution required.".formatted(testFqn.fqn()));
            return true;
        }
        if (coveredClassHasChanged(testFqn)) {
            return true;
        }
        LOGGER.debug("%s: No changes in test or covered classes detected. Execution skipped.".formatted(testFqn.fqn()));
        return false;
    }

    private boolean coveredClassHasChanged(FullyQualifiedClassName test) {
        var changeClasses = analyzedClasses.getChangedClasses();
        for (var coveredClass : testImpactAnalysis.getCoveredClasses(test)) {
            if (changeClasses.contains(coveredClass)) {
                LOGGER.debug("%s: Bytecode change in covered class '%s' detected. Execution required.".formatted(
                        test.fqn(),
                        coveredClass.fqn()
                ));
                return true;
            }
        }
        return false;
    }

}