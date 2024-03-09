package io.skippy.build;

import io.skippy.common.SkippyFolder;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.SessionInfoStore;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;

class ExecFileReader {

    List<Path> getExecutionDataFiles(Path projectDir) {
        File[] files = SkippyFolder.get(projectDir).toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".exec"));
        return asList(files).stream()
                .map(File::toPath).toList();
    }

    List<String> getCoveredClasses(Path execFile) {
        try {
            var coveredClasses = new LinkedList<String>();
            ExecutionDataReader executionDataReader = new ExecutionDataReader(new ByteArrayInputStream(Files.readAllBytes(execFile)));
            executionDataReader.setSessionInfoVisitor(new SessionInfoStore());
            executionDataReader.setExecutionDataVisitor(visitor -> coveredClasses.add(visitor.getName().replace("/", ".").trim()));
            executionDataReader.read();
            return coveredClasses;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    byte[] read(Path execFile) {
        try {
            return Files.readAllBytes(execFile);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
