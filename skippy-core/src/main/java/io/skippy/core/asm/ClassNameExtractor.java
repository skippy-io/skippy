package io.skippy.core.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Extracts the fully-qualified class name (e.g., com.example.Foo) from a class file.
 */
public final class ClassNameExtractor {

    /**
     * Extracts the fully-qualified class name (e.g., com.example.Foo) from the {@param classFile}.
     *
     * @param classFile a class file
     * @return the fully-qualified class name (e.g., com.example.Foo) of the {@param classFile}
     */
    public static String getFullyQualifiedClassName(Path classFile) {
        var className = new AtomicReference<String>();
        try (var inputStream = new FileInputStream(classFile.toFile())) {
            new ClassReader(inputStream).accept(createClassVisitor(className), 0);
            return className.get();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ClassVisitor createClassVisitor(AtomicReference<String> className) {
        return new ClassVisitor(Opcodes.ASM9) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                className.set(name.replace('/', '.'));
            }
        };
    }

}