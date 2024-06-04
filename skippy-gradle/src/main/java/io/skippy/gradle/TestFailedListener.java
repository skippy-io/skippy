package io.skippy.gradle;

import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestListener;
import org.gradle.api.tasks.testing.TestResult;

import java.util.ArrayList;
import java.util.List;

class TestFailedListener implements TestListener {
    final List<TestDescriptor> failedTests = new ArrayList<>();

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
