package io.skippy.core.model;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

/**
 * A mapping between individual tests and the classes that are covered by each test.
 */
public class TestImpactAnalysis {

    public static final TestImpactAnalysis UNAVAILABLE = new TestImpactAnalysis(emptyMap());

    private final Map<FullyQualifiedClassName, List<FullyQualifiedClassName>> testCoverage;

    public TestImpactAnalysis(Map<FullyQualifiedClassName, List<FullyQualifiedClassName>> testCoverage) {
        this.testCoverage = testCoverage;
    }

    List<FullyQualifiedClassName> getCoveredClasses(FullyQualifiedClassName test) {
        return testCoverage.getOrDefault(test, emptyList());
    }

    boolean noDataAvailableFor(FullyQualifiedClassName test) {
        return ! testCoverage.containsKey(test);
    }
}
