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

package io.skippy.gradle.collector;

import io.skippy.gradle.model.ClassFile;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.skippy.gradle.collector.CollectorTestUtils.mockSourceSetContainer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ClassFileCollector}.
 *
 * @author Florian McKee
 */
public class ClassFileCollectorTest {

    @Test
    void testCollect() throws URISyntaxException {

        var currentDir = Paths.get(getClass().getResource("sourceset1").toURI()).toFile();
        var project = mock(Project.class);
        when(project.getProjectDir()).thenReturn(currentDir.getParentFile().getParentFile());

        var classFileCollector = new ClassFileCollector(project, mockSourceSetContainer("sourceset1", "sourceset2"));

        var classFiles = classFileCollector.collect();
        assertEquals(4, classFiles.size());

        var classFile0 = classFiles.get(0);
        assertEquals("com.example.NormalClass1", classFile0.getFullyQualifiedClassName());
        assertEquals(Path.of("collector/sourceset1/NormalClass1.class"), classFile0.getRelativePath());

        var classFile1 = classFiles.get(1);
        assertEquals("com.example.NormalClass2", classFile1.getFullyQualifiedClassName());
        assertEquals(Path.of("collector/sourceset2/NormalClass2.class"), classFile1.getRelativePath());

        var classFile2 = classFiles.get(2);
        assertEquals("com.example.SkippifiedTest1", classFile2.getFullyQualifiedClassName());
        assertEquals(Path.of("collector/sourceset1/SkippifiedTest1.class"), classFile2.getRelativePath());

        var classFile3 = classFiles.get(3);
        assertEquals("com.example.SkippifiedTest2", classFile3.getFullyQualifiedClassName());
        assertEquals(Path.of("collector/sourceset2/SkippifiedTest2.class"), classFile3.getRelativePath());

    }

}
