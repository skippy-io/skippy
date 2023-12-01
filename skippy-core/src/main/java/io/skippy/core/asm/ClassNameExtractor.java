/*
 * Copyright 2023 the original author or authors.
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
 *
 * @author Florian McKee
 */
public final class ClassNameExtractor {

    /**
     * Extracts the fully-qualified class name (e.g., com.example.Foo) from the {@code classFile}.
     *
     * @param classFile a class file
     * @return the fully-qualified class name (e.g., com.example.Foo) of the {@code classFile}
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