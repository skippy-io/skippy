package io.skippy.extension;

import io.skippy.core.SkippyRepositoryExtension;
import io.skippy.core.TestImpactAnalysis;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;

/**
 * Custom {@link SkippyRepositoryExtension} that
 * <ul>
 *     <li>stores and retrieves all data in / from the .skippy folder in the user's home directory,</li>
 *     <li>permanently retains all {@link TestImpactAnalysis} instances and </li>
 *     <li>permanently retains all JaCoCo execution data files.</li>
 * </ul>
 * This implementation serves as simple example for how to implement of a custom {@link SkippyRepositoryExtension}.
 */
public class FileSystemBackedRepositoryExtension implements SkippyRepositoryExtension  {

    private final Path storageFolder = Path.of(System.getProperty("user.home")).resolve(".skippy");

    /**
     * Constructor used by Skippy.
     *
     * @param projectDir the project directory
     */
    public FileSystemBackedRepositoryExtension(Path projectDir) {
        try {
            if (false == exists(storageFolder)) {
                createDirectories(storageFolder);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create new instance: %s".formatted(e.getMessage()), e);
        }
    }

    @Override
    public Optional<TestImpactAnalysis> findTestImpactAnalysis(String id) {
        try {
            var file = storageFolder.resolve("%s.json".formatted(id));
            if (false == exists(file)) {
                return Optional.empty();
            }
            return Optional.of(TestImpactAnalysis.parse(Files.readString(file, StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create new instance: %s".formatted(e.getMessage()), e);
        }
    }

    @Override
    public void saveTestImpactAnalysis(TestImpactAnalysis testImpactAnalysis) {
        try {
            var jsonFile = storageFolder.resolve(Path.of("%s.json".formatted(testImpactAnalysis.getId())));
            Files.writeString(jsonFile, testImpactAnalysis.toJson(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to save test impact analysis %s: %s.".formatted(testImpactAnalysis.getId(), e.getMessage()), e);
        }
    }

    @Override
    public Optional<byte[]> findJacocoExecutionData(String testExecutionId) {
        try {
            var file = storageFolder.resolve("%s.exec".formatted(testExecutionId));
            if (false == exists(file)) {
                return Optional.empty();
            }
            return Optional.of(Files.readAllBytes(file));
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read JaCoCo execution data %s: %s.".formatted(testExecutionId, e.getMessage()), e);
        }
    }

    @Override
    public void saveJacocoExecutionData(String testExecutionId, byte[] jacocoExecutionData) {
        try {
            var file = storageFolder.resolve("%s.exec".formatted(testExecutionId));
            Files.write(file, jacocoExecutionData, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to save JaCoCo execution data %s: %s.".formatted(testExecutionId, e.getMessage()), e);
        }
    }
}
