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

package io.skippy.junit;

import io.skippy.core.SkippyConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

import static io.skippy.junit.SkippyAnalysis.Reason.*;

/**
 * The result of a Skippy analysis:
 * <ul>
 *     <li>a {@link HashedClass} list</li>
 *     <li>a {@link CoverageData} for all tests that use Skippy</li>
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
        NO_HASH_FOR_COVERED_CLASS,
        BYTECODE_CHANGE_IN_COVERED_CLASS
    }

    enum Decision {
        EXECUTE_TEST,
        SKIP_TEST
    }

    record DecisionWithReason(Decision decision, Reason reason) {
        static DecisionWithReason executeTest(Reason reason) {
            return new DecisionWithReason(Decision.EXECUTE_TEST, reason);
        }
        static DecisionWithReason skipTest(Reason reason) {
            return new DecisionWithReason(Decision.SKIP_TEST, reason);
        }
    }


    private static final Logger LOGGER = LogManager.getLogger(SkippyAnalysis.class);
    private static final SkippyAnalysis UNAVAILABLE = new SkippyAnalysis(HashedClasses.UNAVAILABLE, CoverageData.UNAVAILABLE);
    private static final SkippyAnalysis INSTANCE = parse(SkippyConstants.SKIPPY_DIRECTORY);

    private final HashedClasses hashedClasses;
    private final CoverageData coverageData;

    /**
     * C'tor.
     *
     * @param hashedClasses in-memory representation of the {@code classes.md5} file
     * @param coverageData in-memory representation of the {@code .cov} files in the skippy folder
     */
    private SkippyAnalysis(HashedClasses hashedClasses, CoverageData coverageData) {
        this.hashedClasses = hashedClasses;
        this.coverageData = coverageData;
    }

    /**
     * Returns the parsed content of the skippy folder.
     *
     * @return the parsed content of the skippy folder
     */
    public static SkippyAnalysis getInstance() {
        return INSTANCE;
    }

    static SkippyAnalysis parse(Path skippyDirectory) {
        return Profiler.profile("SkippyAnalysis#parse", () -> {
            if (!skippyDirectory.toFile().exists() || !skippyDirectory.toFile().isDirectory()) {
                return SkippyAnalysis.UNAVAILABLE;
            }
            var classFiles = HashedClasses.parse(skippyDirectory.resolve(SkippyConstants.CLASSES_MD5_FILE));
            var testCoverage = CoverageData.parse(skippyDirectory);
            return new SkippyAnalysis(classFiles, testCoverage);
        });
    }

    /**
     * Returns {@code true} if {@code test} needs to be executed, {@code false} otherwise.
     *
     * @param test a class object representing a test
     * @return {@code true} if {@code test} needs to be executed, {@code false} otherwise
     */
    public boolean testNeedsToBeExecuted(Class<?> test) {
        try {
            return decide(new FullyQualifiedClassName(test.getName())).decision == Decision.EXECUTE_TEST;
        } finally {
            Profiler.dump();
        }
    }

    DecisionWithReason decide(FullyQualifiedClassName testFqn) {
        return Profiler.profile("SkippyAnalysis#decide", () -> {
            if (coverageData.noDataAvailableFor(testFqn)) {
                LOGGER.info("%s: No coverage data found: Execution required".formatted(testFqn));
                return DecisionWithReason.executeTest(NO_COVERAGE_DATA_FOR_TEST);
            }
            if (hashedClasses.noDataFor(testFqn)) {
                LOGGER.info("%s: No hash found: Execution required".formatted(testFqn));
                return DecisionWithReason.executeTest(NO_HASH_FOR_TEST);
            }
            if (hashedClasses.hasChanged(testFqn)) {
                LOGGER.info("%s: Bytecode change detected: Execution required".formatted(testFqn));
                return DecisionWithReason.executeTest(BYTECODE_CHANGE_IN_TEST);
            }
            return decideBasedOnCoveredClasses(testFqn);
        });
    }

    private DecisionWithReason decideBasedOnCoveredClasses(FullyQualifiedClassName testFqn) {
        for (var coveredClassFqn : coverageData.getCoveredClasses(testFqn)) {
            if (hashedClasses.hasChanged(coveredClassFqn)) {
                LOGGER.info("%s: Bytecode change in covered class '%s' detected: Execution required".formatted(
                        testFqn.fqn(),
                        coveredClassFqn.fqn()
                ));
                return DecisionWithReason.executeTest(BYTECODE_CHANGE_IN_COVERED_CLASS);
            }
            if (hashedClasses.noDataFor(coveredClassFqn)) {
                LOGGER.info("%s: No hash for covered class '%s' found: Execution required".formatted(
                        testFqn.fqn(),
                        coveredClassFqn.fqn()
                ));
                return DecisionWithReason.executeTest(NO_HASH_FOR_COVERED_CLASS);
            }
        }
        LOGGER.info("%s: No changes in test or covered classes detected: Execution skipped".formatted(testFqn));
        return DecisionWithReason.skipTest(Reason.NO_CHANGE);
    }

}