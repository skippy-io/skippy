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

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HashUtilTest {

    @Test
    void testHashWith32Digits()  {
        assertEquals("ACBD18DB4CC2F85CEDEF654FCCC4A4D8", HashUtil.hashWith32Digits("foo".getBytes(StandardCharsets.UTF_8)));
        assertEquals("37B51D194A7513E45B56F6524F2D51F2", HashUtil.hashWith32Digits("bar".getBytes(StandardCharsets.UTF_8)));
        assertEquals("D41D8CD98F00B204E9800998ECF8427E", HashUtil.hashWith32Digits(new byte[] {}));
    }

    @Test
    void testDebugAgnosticHashOriginalClass() throws URISyntaxException {
        var classFile = Path.of(getClass().getResource("StringUtils.class").toURI());
        assertEquals("ECE5D94D", HashUtil.debugAgnosticHash(classFile));
    }

    @Test
    void testDebugAgnosticHashOriginalClassWithNewComment() throws URISyntaxException {
        var classFile = Path.of(getClass().getResource("StringUtilsWithComment.class").toURI());
        assertEquals("ECE5D94D", HashUtil.debugAgnosticHash(classFile));
    }

    @Test
    void testDebugAgnosticHashOriginalClassWithNewAnnotation() throws URISyntaxException {
        var classFile = Path.of(getClass().getResource("StringUtilsWithAnnotation.class").toURI());
        assertEquals("17D14E41", HashUtil.debugAgnosticHash(classFile));
    }

}
