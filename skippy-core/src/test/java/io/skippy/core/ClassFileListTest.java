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

package io.skippy.core;

import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link ClassFileList}.
 *
 * @author Florian McKee
 */
public class ClassFileListTest {

    @Test
    void testParse() throws URISyntaxException {
        var analyzedFilesTxt = Path.of(getClass().getResource("analyzedclasslist/analyzedFiles.txt").toURI());
        var analyzedFiles = ClassFileList.parse(analyzedFilesTxt);

        assertEquals(asList(
            new FullyQualifiedClassName("io.skippy.core.AnalyzedClass"),
            new FullyQualifiedClassName("io.skippy.core.AnalyzedClassList")
        ), analyzedFiles.getClasses());
    }

}