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

package io.skippy.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.testing.Test;

import javax.inject.Inject;

import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestListener;
import org.gradle.api.tasks.testing.TestResult;

import java.util.ArrayList;
import java.util.List;

import static io.skippy.gradle.SkippyGradleUtils.*;

/**
 * Informs Skippy that the relevant parts of the build (e.g., compilation and testing) have finished.
 */
class SkippyAnalyzeTask extends DefaultTask {

    @Inject
    public SkippyAnalyzeTask() {
        setGroup("skippy");
        var testFailedListener = new TestFailedListener();
        getProject().getTasks().withType(Test.class, testTask -> testTask.addTestListener(testFailedListener));
        doLast(task -> {
            ifBuildSupportsSkippy(getProject(), skippyBuildApi -> {
                for (var failedTest : testFailedListener.failedTests) {
                    skippyBuildApi.testFailed(failedTest.getClassName());
                }
                skippyBuildApi.buildFinished();
            });
        });
    }

    static private class TestFailedListener implements TestListener {
        private final List<TestDescriptor> failedTests = new ArrayList<>();

        @Override
        public void beforeSuite(TestDescriptor testDescriptor) {
        }

        @Override
        public void afterSuite(TestDescriptor testDescriptor, TestResult testResult) {
        }

        @Override
        public void beforeTest(TestDescriptor testDescriptor) {
        }

        @Override
        public void afterTest(TestDescriptor testDescriptor, TestResult testResult) {
            if (testResult.getResultType() == TestResult.ResultType.FAILURE) {
                failedTests.add(testDescriptor);
            }
        }
    }

}