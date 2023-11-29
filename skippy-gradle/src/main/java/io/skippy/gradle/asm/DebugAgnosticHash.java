package io.skippy.gradle.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Hash that is agnostic of debug information in the class file like LineNumberTable attributes
 * (https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.7.12). That means the hash will stay the
 * same if the only thing that changes is debug information (e.g., due to a newline or comment in the class file).
 */
public class DebugAgnosticHash {

    /**
     * Returns that is agnostic of debug information in the class file.
     *
     * @param classFile a class file
     * @return a hash of the class file that is agnostic of debug information
     */
    public static String hash(Path classFile) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(getBytecodeWithoutDebugInformation(classFile));
            return Base64.getEncoder().encodeToString(md.digest());
        } catch (Exception e) {
            throw new RuntimeException("Unable to generate hash for file '%s': '%s'".formatted(classFile, e.getMessage()), e);
        }
    }

    private static byte[] getBytecodeWithoutDebugInformation(Path classFile) {
        try (var inputStream = new FileInputStream(classFile.toFile())) {
            var classWriter = new ClassWriter(Opcodes.ASM9);
            var classVisitor = new ClassVisitor(Opcodes.ASM9, classWriter) {};
            new ClassReader(inputStream).accept(classVisitor, ClassReader.SKIP_DEBUG);
            return classWriter.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
