package io.skippy.core.parser;

import io.skippy.core.model.FullyQualifiedClassName;
import io.skippy.core.model.SourceFileSnapshot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

class SourceFileSnapshotParser {

    private static final Logger LOGGER = LogManager.getLogger(SourceFileSnapshotParser.class);

    static List<SourceFileSnapshot> parse(Path file) {
        if (!file.toFile().exists()) {
            return emptyList();
        }
        try {
            var result = new ArrayList<SourceFileSnapshot>();
            for (var line : Files.readAllLines(file, Charset.forName("UTF8"))) {
                String[] split = line.split(":");
                result.add(new SourceFileSnapshot(new FullyQualifiedClassName(split[0]), Path.of(split[1]), Path.of(split[2]), split[3], split[4]));
            }
            return result;
        } catch (Exception e) {
            LOGGER.error("Parsing of file '%s' failed: '%s'".formatted(file, e.getMessage()), e);
            throw new RuntimeException(e);
        }
    }

}