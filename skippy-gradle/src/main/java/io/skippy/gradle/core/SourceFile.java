package io.skippy.gradle.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.regex.Pattern.quote;

/**
 * Skippy representation of a source file:
 * <ul>
 *     <li>a fully-qualified class name</li>
 *     <li>the path to the source file in the file system</li>
 *     <li>the path to the corresponding class file in the file system</li>
 * </ul>>
 *
 * TODO: Ideally, the corresponding Class object should be stored as well to detect the Skippy extension and to infer the
 * fully qualified class name.
 */
public class SourceFile {

    private static final List<String> SKIPPY_EXTENSION_PATTERNS = asList(
            "@ExtendWith(Skippy.class)",
            "@ExtendWith(io.skippy.junit5.Skippy.class)",
            "@org.junit.jupiter.api.extension.ExtendWith(Skippy.class)",
            "@org.junit.jupiter.api.extension.ExtendWith(io.skippy.junit5.Skippy.class)"
    );

    private static final Function<String,String> REMOVE_WHITESPACES = s -> s.replaceAll("\\s", "");

    private final String fullyQualifiedClassName;
    private final Path sourceFile;
    private final Path classFile;

    /**
     * C'tor.
     *
     */
    private SourceFile(String fullyQualifiedClassName, Path sourceFile, Path classFile) {
        this.fullyQualifiedClassName = fullyQualifiedClassName;
        this.sourceFile = sourceFile;
        this.classFile = classFile;
    }

    public static SourceFile of(Path sourceFile, Path sourceFolder, Path classesFolder) {
        var pathWithExtension = sourceFolder.relativize(sourceFile.toAbsolutePath()).toString();
        var fullyQualifiedClassName = pathWithExtension
                .substring(0, pathWithExtension.lastIndexOf("."))
                .replaceAll("/", ".");
        var classFile = classesFolder.resolve(Path.of(fullyQualifiedClassName.replaceAll(quote("."), "/") + ".class"));
        return new SourceFile(fullyQualifiedClassName, sourceFile, classFile);
    }

    /**
     * Returns the fully qualified class name (e.g., com.example.Foo).
     */
    public String getFullyQualifiedClassName() {
        return fullyQualifiedClassName;
    }

    /**
     * Returns the fully-qualified filename of the source file (e.g., /user/johndoe/repos/demo/src/main/java/com/example/Foo.java).
     */
    public String getSourceFileName() {
        return sourceFile.toString();
    }

    /**
     * Returns the fully-qualified filename of the class file (e.g., /user/johndoe/repos/demo/build/classes/java/main/com/example/Foo.class).
     */
    public String getClassFileName() {
        return classFile.toString();
    }

    /**
     * Returns the MD5 hash of the Java source file in BASE64 encoding.
     */
    public String getSourceFileHash() {
        return getHash(sourceFile);
    }

    /**
     * Returns the MD5 hash of the Java class file in BASE64 encoding.
     */
    public String getClassFileHash() {
        return getHash(classFile);
    }

    /**
     * Returns <code>true</code> if this class is the test that uses the Skippy extension, <code>false</code> otherwise.
     */
    public boolean usesSkippyExtension() {
        try {
            var fileContent = Files.readAllLines(sourceFile).stream()
                    .map(REMOVE_WHITESPACES)
                    .toList();
            for (var pattern : SKIPPY_EXTENSION_PATTERNS) {
                for (var line : fileContent) {
                    if (line.startsWith(pattern)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getHash(Path file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(Files.readAllBytes(file));
            return Base64.getEncoder().encodeToString(md.digest());
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
    }


}
