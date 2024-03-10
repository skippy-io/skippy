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

package io.skippy.common.util;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hash functions used throughout Skippy.
 *
 * Note: None of the methods in this class are intended for security-related purposes.
 *
 * @author Florian McKee
 */
public final class HashUtil {

    /**
     * Generates a 32-digit hexadecimal hash of the input.
     *
     * @param data the input
     * @return a 32-digit hexadecimal hash of the input
     */
    public static String hashWith32Digits(byte[] data) {
        return fullHash(data);
    }

    /**
     * Generates a 8-digit hexadecimal hash for the {@code classfile} that is agnostic of debug information.
     *
     * If the only difference between two class files is debug information within the bytecode, their hash will be the same.
     * <br /><br />
     * This allows Skippy to treat certain changes like
     * <ul>
     *     <li>change in formatting and indentation,</li>
     *     <li>updated JavaDocs and</li>
     *     <li>addition of newlines and linebreaks</li>
     * </ul>
     * as 'no-ops'.
     *
     * @param classFile a class file
     * @return a 8-digit hexadecimal  hash of the {@code classfile} that is agnostic of debug information
     */
    public static String debugAgnosticHash(Path classFile) {
        return fullHash(getBytecodeWithoutDebugInformation(classFile)).substring(24, 32);
    }

    private static String fullHash(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data);
            return bytesToHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Generation of hash failed.", e);
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

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString().toUpperCase();
    }
}
