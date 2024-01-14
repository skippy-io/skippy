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

package io.skippy.gradle;

import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.SourceSetOutput;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link GradleClassFileCollector}.
 *
 * @author Florian McKee
 */
public class GradleClassFileCollectorTest {

    @Test
    void testCollect() {

        var sourceSetContainer = mockSourceSetContainer("sourceset1", "sourceset2");

        var classFileCollector = new GradleClassFileCollector(sourceSetContainer);

        var directoriesWithClassFiles = classFileCollector.collect();

        assertEquals(2, directoriesWithClassFiles.size());

        var dir0 = directoriesWithClassFiles.get(0);
        var dir1 = directoriesWithClassFiles.get(1);

        assertEquals(2, dir0.classFiles().size());
        assertEquals(2, dir1.classFiles().size());

        assertEquals("com.example.NormalClass1", dir0.classFiles().get(0).getFullyQualifiedClassName());
        assertEquals(Path.of("sourceset1/NormalClass1.class"), dir0.directory().getParent().relativize(dir0.classFiles().get(0).getAbsolutePath()));

        assertEquals("com.example.SkippifiedTest1", dir0.classFiles().get(1).getFullyQualifiedClassName());
        assertEquals(Path.of("sourceset1/SkippifiedTest1.class"), dir0.directory().getParent().relativize(dir0.classFiles().get(1).getAbsolutePath()));


        assertEquals("com.example.NormalClass2", dir1.classFiles().get(0).getFullyQualifiedClassName());
        assertEquals(Path.of("sourceset2/NormalClass2.class"), dir1.directory().getParent().relativize(dir1.classFiles().get(0).getAbsolutePath()));

        assertEquals("com.example.SkippifiedTest2", dir1.classFiles().get(1).getFullyQualifiedClassName());
        assertEquals(Path.of("sourceset2/SkippifiedTest2.class"), dir1.directory().getParent().relativize(dir1.classFiles().get(1).getAbsolutePath()));
    }


    private static SourceSetContainer mockSourceSetContainer(String... sourceSetDirectories) {
        var sourceSetContainer = mock(SourceSetContainer.class);
        var sourceSets = asList(sourceSetDirectories).stream().map(GradleClassFileCollectorTest::mockSourceSet).toList();
        for (int i = 0; i < sourceSets.size(); i++) {
            when(sourceSetContainer.getByName(sourceSetDirectories[i])).thenReturn(sourceSets.get(i));
        }
        when(sourceSetContainer.iterator()).thenReturn(sourceSets.iterator());
        return sourceSetContainer;
    }

    private static SourceSet mockSourceSet(String directory) {
        try {
            File outputDirectory = Paths.get(GradleClassFileCollectorTest.class.getResource(directory).toURI()).toFile();
            var sourceSet = mock(SourceSet.class);
            var sourceSetOutput = mock(SourceSetOutput.class);
            when(sourceSet.getOutput()).thenReturn(sourceSetOutput);
            var classesDir = mock(FileCollection.class);
            when(sourceSetOutput.getClassesDirs()).thenReturn(classesDir);
            when(classesDir.getFiles()).thenReturn(Set.of(outputDirectory));
            return sourceSet;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
