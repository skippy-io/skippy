/*
 * Copyright 2023-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.skippy.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.file.Files.newInputStream;

/**
 * Static utility methods that primarily operate on {@link Class} objects.
 *
 * @author Florian McKee
 */
class ClassUtil {

    /**
     * Return the output folder of {@code clazz} relative to the {@code projectFolder}.
     *
     * @param projectFolder (e.g. /home/foo/repos/my-project)
     * @param clazz (e.g., com.example.Foo)
     * @return the output folder of {@code clazz} relative to the {@code projectFolder} (e.g., build/classes/java/test)
     */
    static Path getOutputFolder(Path projectFolder, Class<?> clazz) {
        try {
            return projectFolder.toAbsolutePath().relativize((Path.of(clazz.getProtectionDomain().getCodeSource().getLocation().toURI())));
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to obtain output folder for class %s in project %s: %s".formatted(clazz.getName(), projectFolder, e), e);
        }
    }

    /**
     * Returns {@code true} if the location for {@code clazz} is available, {@code false} otherwise.
     *
     * @param clazz a {@link Class} object
     * @return {@code true} if the location for {@code clazz} is available, {@code false} otherwise
     */
    static boolean locationAvailable(Class<?> clazz) {
        try {
            return clazz.getProtectionDomain().getCodeSource().getLocation() != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extracts the fully-qualified class name (e.g., com.example.Foo) from the {@code classFile}.
     *
     * @param classFile a class file
     * @return the fully-qualified class name (e.g., com.example.Foo) of the {@code classFile}
     */
    static String getFullyQualifiedClassName(Path classFile) {
        var className = new AtomicReference<String>();
        try (var inputStream = newInputStream(classFile)) {
            new ClassReader(inputStream).accept(createClassVisitor(className), 0);
            return className.get();
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to obtain fully qualified class name from class file %s.".formatted(classFile), e);
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
