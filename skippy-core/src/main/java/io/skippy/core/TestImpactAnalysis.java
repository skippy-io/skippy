package io.skippy.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

/**
 * A mapping between tests and the classes they cover.
 */
public class TestImpactAnalysis {

    private static final Logger LOGGER = LogManager.getLogger(TestImpactAnalysis.class);

    /**
     * Indicates that no test impact analysis was found.
     */
    public static final TestImpactAnalysis UNAVAILABLE = new TestImpactAnalysis(emptyMap());

    private final Map<FullyQualifiedClassName, List<FullyQualifiedClassName>> testCoverage;

    /**
     * C'tor.
     *
     * @param testCoverage a mapping between tests and the classes that they cover
     */
    private TestImpactAnalysis(Map<FullyQualifiedClassName, List<FullyQualifiedClassName>> testCoverage) {
        this.testCoverage = testCoverage;
    }

    static TestImpactAnalysis parse(Path directory) {
        var result = new HashMap<FullyQualifiedClassName, List<FullyQualifiedClassName>>();
        for (var csvFile : directory.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"))) {
            var test = new FullyQualifiedClassName(toClassName(csvFile.toPath()));
            result.put(test, parseCsvFile(csvFile.toPath()));
        }
        return new TestImpactAnalysis(result);
    }

    /**
     * Returns the classes that are covered by {@param test}.
     *
     * @param test a {@link FullyQualifiedClassName} representing a test
     * @return a list of {@link FullyQualifiedClassName}s representing the classes that are covered by {@param test}
     */
    List<FullyQualifiedClassName> getCoveredClasses(FullyQualifiedClassName test) {
        return testCoverage.getOrDefault(test, emptyList());
    }

    /**
     * Returns {@code true} if no coverage data is available for {@param test}, {@code false} otherwise.
     *
     * @param test a {@link FullyQualifiedClassName} representing a test
     * @return {@code true} if no coverage data is available for {@param test}, {@code false} otherwise
     */
    boolean noDataAvailableFor(FullyQualifiedClassName test) {
        return ! testCoverage.containsKey(test);
    }

    private static List<FullyQualifiedClassName> parseCsvFile(Path csvFile) {
        try {
            var content = new ArrayList<>(Files.readAllLines(csvFile));
            // remove header
            content.remove(0);
            var coveredClasses = new ArrayList<FullyQualifiedClassName>();
            for (var line : content) {
                coveredClasses.addAll(parseCsvFile(line));
            }
            return coveredClasses;
        } catch (Exception e) {
            LOGGER.error("Parsing of file '%s' failed: '%s'".formatted(csvFile, e), e);
            throw new RuntimeException(e);
        }
    }

    private static List<FullyQualifiedClassName> parseCsvFile(String line) {
        var csvColumns = line.split(",");
        var methodsCoveredCount = parseInt(csvColumns[12]);
        var pkg = csvColumns[1];
        var clazz = csvColumns[2];
        if (methodsCoveredCount > 0) {
            return asList(new FullyQualifiedClassName(pkg + "." + clazz));
        }
        return emptyList();
    }

    private static String toClassName(Path csvFile) {

        // /a/b/c/com_example_Foo.csv -> com_example_Foo.csv
        var filename = csvFile.getFileName().toString();

        // com_example_Foo.csv -> com_example_Foo
        var withoutExtension = filename.substring(0, filename.length() - 4);

        // com_example_Foo -> com.example.Foo
        return withoutExtension.replaceAll("_", ".");
    }
}
