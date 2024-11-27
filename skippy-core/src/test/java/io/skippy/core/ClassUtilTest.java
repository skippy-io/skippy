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

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link ClassUtil}.
 *
 * @author Florian McKee
 */
public class ClassUtilTest {

    @Test
    void testFetFullyQualifiedClassName() {
        var classFile = Path.of("build/classes/java/test").resolve("com/example/StringUtils.class");
        assertEquals("com.example.StringUtils", ClassUtil.getFullyQualifiedClassName(classFile));
    }

    @Test
    void testGetOutputFolder() throws Exception {
        assertEquals(Path.of("build/classes/java/test"), ClassUtil.getOutputFolder(Path.of(""), Class.forName("com.example.LeftPadderTest")));
    }

    @Test
    void testLocationAvailable() throws Exception {
        assertEquals(true, ClassUtil.locationAvailable(Class.forName("com.example.LeftPadderTest")));
    }

    @Test
    void testLocationAvailableUnavailable() throws Exception {

        // the class loading gymnastics below create a Class object w/o location information
        var location = Class.forName("com.example.LeftPadderTest").getProtectionDomain().getCodeSource().getLocation();
        var pathToClassFile = Path.of(location.toURI()).resolve("com").resolve("example").resolve("LeftPadderTest.class");

        class ByteArrayClassLoader extends ClassLoader {
            public Class<?> loadClassFromBytes(String className, byte[] classBytes) {
                return defineClass(className, classBytes, 0, classBytes.length);
            }
        }

        var testClass = new ByteArrayClassLoader().loadClassFromBytes("com.example.LeftPadderTest", Files.readAllBytes(pathToClassFile));
        assertEquals(false, ClassUtil.locationAvailable(testClass));
    }
}