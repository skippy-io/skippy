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

import static io.skippy.core.SkippyAnalysis.Reason.*;

/**
 * The result of a Skippy analysis:
 * <ul>
 *     <li>a {@link ClassFile} list</li>
 *     <li>a {@link TestImpactAnalysis} for all tests that use the Skippy extension</li>
 * </ul>
 *
 * @author Florian McKee
 */
public class SkippyAnalysis {

    enum Reason {
        NO_CHANGE,
        NO_COVERAGE_DATA_FOR_TEST,
        BYTECODE_CHANGE_IN_TEST,
        NO_HASH_FOR_TEST,
        NO_HASH_FOR_COVERED_CLASS, BYTECODE_CHANGE_IN_COVERED_CLASS
    }

    enum Decision {
        EXECUTE_TEST,
        SKIP_TEST
    }

    record DecisionWithReason(Decision decision, Reason reason) {
        static DecisionWithReason execute(Reason reason) {
            return new DecisionWithReason(Decision.EXECUTE_TEST, reason);
        }
        static DecisionWithReason skip(Reason reason) {
            return new DecisionWithReason(Decision.SKIP_TEST, reason);
        }
    }

    private static final Logger LOGGER = LogManager.getLogger(SkippyAnalysis.class);

    private static final SkippyAnalysis UNAVAILABLE = new SkippyAnalysis(ClassFileList.UNAVAILABLE, TestImpactAnalysis.UNAVAILABLE);

    private final ClassFileList classFiles;
    private final TestImpactAnalysis testImpactAnalysis;

    /**
     * C'tor.
     *
     * @param classFiles all classes that have been analyzed by Skippy's analysis
     * @param testImpactAnalysis the {@link TestImpactAnalysis} created by Skippy's analysis
     */
    SkippyAnalysis(ClassFileList classFiles, TestImpactAnalysis testImpactAnalysis) {
        this.classFiles = classFiles;
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
        var classFiles = ClassFileList.parse(skippyDirectory.resolve(SkippyConstants.CLASSES_MD5_FILE));
        var testCoverage = TestImpactAnalysis.parse(skippyDirectory);
        return new SkippyAnalysis(classFiles, testCoverage);
    }

    /**
     * Returns {@code true} if {@code test} needs to be executed, {@code false} otherwise.
     *
     * @param test a class object representing a test
     * @return {@code true} if {@code test} needs to be executed, {@code false} otherwise
     */
    public boolean execute(Class<?> test) {
        return decide(new FullyQualifiedClassName(test.getName())).decision == Decision.EXECUTE_TEST;
    }

    DecisionWithReason decide(FullyQualifiedClassName testFqn) {
        if (testImpactAnalysis.noDataAvailableFor(testFqn)) {
            LOGGER.debug("%s: No coverage data found: Execution required".formatted(testFqn));
            return DecisionWithReason.execute(NO_COVERAGE_DATA_FOR_TEST);
        }
        if (classFiles.noDataFor(testFqn)) {
            LOGGER.debug("%s: No hash found: Execution required".formatted(testFqn));
            return DecisionWithReason.execute(NO_HASH_FOR_TEST);
        }
        if (classFiles.getChangedClasses().contains(testFqn)) {
            LOGGER.debug("%s: Bytecode change detected: Execution required".formatted(testFqn));
            return DecisionWithReason.execute(BYTECODE_CHANGE_IN_TEST);
        }
        return coveredClassHasChanged(testFqn);
    }

    private DecisionWithReason coveredClassHasChanged(FullyQualifiedClassName testFqn) {
        var changeClasses = classFiles.getChangedClasses();
        for (var coveredClassFqn : testImpactAnalysis.getCoveredClasses(testFqn)) {
            if (changeClasses.contains(coveredClassFqn)) {
                LOGGER.debug("%s: Bytecode change in covered class '%s' detected: Execution required".formatted(
                        testFqn.fqn(),
                        coveredClassFqn.fqn()
                ));
                return DecisionWithReason.execute(BYTECODE_CHANGE_IN_COVERED_CLASS);
            }
            if (classFiles.noDataFor(coveredClassFqn)) {
                LOGGER.debug("%s: No hash for covered class '%s' found: Execution required".formatted(
                        testFqn.fqn(),
                        coveredClassFqn.fqn()
                ));
                return DecisionWithReason.execute(NO_HASH_FOR_COVERED_CLASS);
            }
        }
        LOGGER.debug("%s: No changes in test or covered classes detected: Execution skipped".formatted(testFqn));
        return DecisionWithReason.skip(Reason.NO_CHANGE);
    }

}