package io.skippy.core.parser;

import io.skippy.core.model.FullyQualifiedClassName;
import io.skippy.core.model.TestImpactAnalysis;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

class TestImpactAnalysisParser {

    private static final Logger LOGGER = LogManager.getLogger(TestImpactAnalysisParser.class);

    static TestImpactAnalysis parse(Path directory) {
        var result = new HashMap<FullyQualifiedClassName, List<FullyQualifiedClassName>>();
        for (var csvFile : directory.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"))) {
            var test = new FullyQualifiedClassName(toClassName(csvFile.toPath()));
            result.put(test, parseCsvFile(csvFile.toPath()));
        }
        return new TestImpactAnalysis(result);
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