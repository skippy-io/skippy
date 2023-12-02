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

import static io.skippy.gradle.Profiler.profile;

/**
 * Generates hashes for class files that are agnostic of debug information. If the only difference between two class
 * files is debug information within the bytecode, their hash will be the same.
 * <br /><br />
 * This allows Skippy to treat certain changes like
 * <ul>
 *     <li>change in formatting and indentation,</li>
 *     <li>updated JavaDocs and</li>
 *     <li>addition of newlines and linebreaks</li>
 * </ul>
 * as 'no-ops'.
 *
 * @author Florian McKee
 */
public class DebugAgnosticHash {

    /**
     * Generates a hash for the {@code classfile} that is agnostic of debug information.
     *
     * @param classFile a class file
     * @return a hash of the {@code classfile} that is agnostic of debug information
     */
    public static String hash(Path classFile) {
        return profile(DebugAgnosticHash.class, "hash", () -> {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(getBytecodeWithoutDebugInformation(classFile));
                return Base64.getEncoder().encodeToString(md.digest());
            } catch (Exception e) {
                throw new RuntimeException("Unable to generate hash for file '%s': '%s'".formatted(classFile, e.getMessage()), e);
            }
        });
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
