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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.nio.file.Files.newInputStream;

/**
 * Hash functions used throughout Skippy.
 *
 * Note: None of the methods in this class are intended for security-related purposes.
 *
 * @author Florian McKee
 */
final class HashUtil {

    /**
     * Generates a 32-digit hexadecimal hash of the input.
     *
     * @param data the input
     * @return a 32-digit hexadecimal hash of the input
     */
    static String hashWith32Digits(byte[] data) {
        return fullHash(data);
    }

    /**
     * Generates an 8-character hexadecimal hash of the input.
     *
     * @param data the input
     * @return ab 8-character hexadecimal hash of the input
     */
    static String hashWith8Digits(byte[] data) {
        return fullHash(data).substring(24, 32);
    }

    /**
     * Generates an 8-digit hexadecimal hash of the {@code classfile}.
     *
     * @param classFile a class file
     * @return an 8-digit hexadecimal  hash of the {@code classfile}
     */
    static String hashWith8Digits(Path classFile) {
        return hashWith8Digits(readByteCode(classFile));
    }

    private static String fullHash(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data);
            return bytesToHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Generation of hash failed: %s".formatted(e), e);
        }
    }

    private static byte[] readByteCode(Path classFile) {
        try (var inputStream = newInputStream(classFile)) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read bytecode for class file %s: %s".formatted(classFile, e), e);
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
