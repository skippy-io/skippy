/*
 * Copyright 2023-2024 the original author or authors.
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

import io.skippy.core.Profiler;
import io.skippy.core.SkippyConstants;
import io.skippy.core.SkippyUtils;

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
class SkippyAnalysis {

    enum Reason {
        NO_CHANGE,
        NO_COVERAGE_DATA_FOR_TEST,
        BYTECODE_CHANGE_IN_TEST,
        NO_HASH_FOR_TEST,
        NO_HASH_FOR_COVERED_CLASS,
        BYTECODE_CHANGE_IN_COVERED_CLASS
    }

    enum Prediction {
        EXECUTE,
        SKIP
    }

    record PredictionWithReason(Prediction prediction, Reason reason) {
        static PredictionWithReason execute(Reason reason) {
            return new PredictionWithReason(Prediction.EXECUTE, reason);
        }
        static PredictionWithReason skip(Reason reason) {
            return new PredictionWithReason(Prediction.SKIP, reason);
        }
    }

    private static final SkippyAnalysis UNAVAILABLE = new SkippyAnalysis(HashedClasses.UNAVAILABLE, CoverageData.UNAVAILABLE);
    static final SkippyAnalysis INSTANCE = parse(SkippyUtils.getSkippyFolder());

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

    PredictionWithReason predict(FullyQualifiedClassName testFqn) {
        return Profiler.profile("SkippyAnalysis#predict", () -> {
            if (coverageData.noDataAvailableFor(testFqn)) {
                return PredictionWithReason.execute(NO_COVERAGE_DATA_FOR_TEST);
            }
            if (hashedClasses.noDataFor(testFqn)) {
                return PredictionWithReason.execute(NO_HASH_FOR_TEST);
            }
            if (hashedClasses.hasChanged(testFqn)) {
                return PredictionWithReason.execute(BYTECODE_CHANGE_IN_TEST);
            }
            return predictBasedOnCoveredClasses(testFqn);
        });
    }

    private PredictionWithReason predictBasedOnCoveredClasses(FullyQualifiedClassName testFqn) {
        for (var coveredClassFqn : coverageData.getCoveredClasses(testFqn)) {
            if (hashedClasses.hasChanged(coveredClassFqn)) {
                return PredictionWithReason.execute(BYTECODE_CHANGE_IN_COVERED_CLASS);
            }
            if (hashedClasses.noDataFor(coveredClassFqn)) {
                return PredictionWithReason.execute(NO_HASH_FOR_COVERED_CLASS);
            }
        }
        return PredictionWithReason.skip(Reason.NO_CHANGE);
    }

}