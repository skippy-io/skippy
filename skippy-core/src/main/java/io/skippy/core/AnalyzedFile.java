package io.skippy.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * A file that has been analyzed by Skippy:
 * <ul>
 *     <li>a fully-qualified class name</li>
 *     <li>a path to the source file</li>
 *     <li>a path to the class file</li>
 *     <li>MD5 hash of source file</li>
 *     <li>MD5 hash of class file</li>
 * </ul>
 */
public class AnalyzedFile {

    private static final Logger LOGGER = LogManager.getLogger(AnalyzedFile.class);

    private final FullyQualifiedClassName fullyQualifiedClassName;
    private final Path sourceFile;
    private final Path classFile;
    private final String sourceFileHash;
    private final String classFileHash;

    /**
     * C'tor.
     *
     * @param fullyQualifiedClassName the fully-qualified class name (e.g., com.example.Foo)
     * @param sourceFile the source file in the file system (e.g., /User/johndoe/repo/src/main/java/com/example/Foo.java)
     * @param classFile the class file in the file system (e.g., /user/johndoe/repos/demo/build/classes/java/main/com/example/Foo.class)
     * @param sourceFileHash the MD5 hash of the content of the source file (e.g., pz1sZJBt6JArm/LYs+UXKg==)
     * @param classFileHash the MD5 hash of the content of the class file (e.g., YA9ExftvTDku3TUNsbkWIw==)
     */
    private AnalyzedFile(FullyQualifiedClassName fullyQualifiedClassName, Path sourceFile, Path classFile, String sourceFileHash, String classFileHash) {
        this.fullyQualifiedClassName = fullyQualifiedClassName;
        this.sourceFile = sourceFile;
        this.classFile = classFile;
        this.sourceFileHash = sourceFileHash;
        this.classFileHash = classFileHash;
    }

    static List<AnalyzedFile> parse(Path skippyAnalysisFile) {
        if (!skippyAnalysisFile.toFile().exists()) {
            return emptyList();
        }
        try {
            var result = new ArrayList<AnalyzedFile>();
            for (var line : Files.readAllLines(skippyAnalysisFile, Charset.forName("UTF8"))) {
                String[] split = line.split(":");
                result.add(new AnalyzedFile(new FullyQualifiedClassName(split[0]), Path.of(split[1]), Path.of(split[2]), split[3], split[4]));
            }
            return result;
        } catch (Exception e) {
            LOGGER.error("Parsing of file '%s' failed: '%s'".formatted(skippyAnalysisFile, e.getMessage()), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the fully-qualified class name.
      *
     * @return the fully-qualified class name
     */
    FullyQualifiedClassName getFullyQualifiedClassName() {
        return fullyQualifiedClassName;
    }

    /**
     * Returns {@code true} if the source file has changed since it was analyzed, {@code false} otherwise.
     *
     * @return {@code true} if the source file changed since it was analyzed, {@code false} otherwise
     */
    boolean sourceFileHasChanged() {
        if ( ! sourceFile.toFile().exists()) {
            return true;
        }
        String newSourceFileHash = hashFileContent(sourceFile);
        if ( ! sourceFileHash.equals(newSourceFileHash)) {
            return true;
        }
        return false;
    }

    /**
     * Returns {@code true} if the class file has changed since it was analyzed, {@code false} otherwise.
     *
     * @return {@code true} if the class file has changed since it was analyzed, {@code false} otherwise.
     */
    boolean classFileHasChanged() {
        if ( ! classFile.toFile().exists()) {
            return true;
        }
        String newClassFileHash = hashFileContent(classFile);
        if ( ! classFileHash.equals(newClassFileHash)) {
            return true;
        }
        return false;
    }

    private static String hashFileContent(Path file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(Files.readAllBytes(file));
            return Base64.getEncoder().encodeToString(md.digest());
        } catch (Exception e) {
            LOGGER.error("Unable to generate hash for file '%s': '%s'".formatted(file, e.getMessage()), e);
            throw new RuntimeException(e);
        }
    }

}
