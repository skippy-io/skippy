package io.skippy.gradle.asm;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Checks whether a class is annotated with the Skippy JUnit 5 Extension.
 */
public final class SkippyJUnit5Detector {

    /**
     * Returns {@code true} if the {@param classFile} is annotated with the Skippy JUnit 5 Extension, {@code false}
     * otherwise.
     *
     * @param classFile a class file.
     * @return {@code true} if the {@param classFile} is annotated with the Skippy JUnit 5 Extension, {@code false}
     *      otherwise
     */
    public static boolean usesSkippyExtension(Path classFile) {
        var usesSkippyJunit5Extension = new AtomicBoolean(false);
        try (var inputStream = new FileInputStream(classFile.toFile())) {
            new ClassReader(inputStream).accept(createClassVisitor(usesSkippyJunit5Extension), 0);
            return usesSkippyJunit5Extension.get();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @NotNull
    private static ClassVisitor createClassVisitor(AtomicBoolean usesSkippyJunit5Extension) {
        return new ClassVisitor(Opcodes.ASM9) {
            @Override
            public AnnotationVisitor visitAnnotation(String annotationType, boolean visible) {
                return new AnnotationVisitor(Opcodes.ASM9) {
                    @Override
                    public AnnotationVisitor visitArray(final String name) {
                        return new AnnotationVisitor(Opcodes.ASM9) {
                            @Override
                            public void visit(String name, Object value) {
                                if (annotationType.equals("Lorg/junit/jupiter/api/extension/ExtendWith;")) {
                                    if (value instanceof Type type) {
                                        if (type.getClassName().equals("io.skippy.junit5.Skippy")) {
                                            usesSkippyJunit5Extension.set(true);
                                        }
                                    }
                                }
                            }
                        };
                    }
                };
            }
        };
    }

}