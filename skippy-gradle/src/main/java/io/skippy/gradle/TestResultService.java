package io.skippy.gradle;

import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestResult;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

abstract class TestResultService implements BuildService<BuildServiceParameters.None> {

    final List<TestDescriptor> failedTests = new ArrayList<>();

    @Inject
    public TestResultService() {
    }

    void afterTest(TestDescriptor testDescriptor, TestResult testResult) {
        if (testResult.getResultType() == TestResult.ResultType.FAILURE) {
            failedTests.add(testDescriptor);
        }
    }
}
