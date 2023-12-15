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

import org.gradle.api.Project;
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

        var sourceSetContainer = mockSourceSetContainer("sourceset1", "sourceset2");

        var classFileCollector = new ClassFileCollector(project, sourceSetContainer);

        var classFiles = classFileCollector.collect();
        var sourceSet0Path = classFiles.keySet().stream().filter(it -> it.toString().endsWith("sourceset1")).findFirst().get();
        var sourceSet1Path = classFiles.keySet().stream().filter(it -> it.toString().endsWith("sourceset2")).findFirst().get();

        assertEquals(2, classFiles.keySet().size());
        assertEquals(2, classFiles.get(sourceSet0Path).size());
        assertEquals(2, classFiles.get(sourceSet1Path).size());
        

        var sourceSet0Class0 = classFiles.get(sourceSet0Path).get(0);
        assertEquals("com.example.NormalClass1", sourceSet0Class0.getFullyQualifiedClassName());
        assertEquals(Path.of("sourceset1/NormalClass1.class"), sourceSet0Path.getParent().relativize(sourceSet0Class0.getAbsolutePath()));

        var sourceSet0Class1 = classFiles.get(sourceSet0Path).get(1);
        assertEquals("com.example.SkippifiedTest1", sourceSet0Class1.getFullyQualifiedClassName());
        assertEquals(Path.of("sourceset1/SkippifiedTest1.class"), sourceSet0Path.getParent().relativize(sourceSet0Class1.getAbsolutePath()));

        var sourceSet1Class0 = classFiles.get(sourceSet1Path).get(0);
        assertEquals("com.example.NormalClass2", sourceSet1Class0.getFullyQualifiedClassName());
        assertEquals(Path.of("sourceset2/NormalClass2.class"), sourceSet1Path.getParent().relativize(sourceSet1Class0.getAbsolutePath()));

        var sourceSet1Class1 = classFiles.get(sourceSet1Path).get(1);
        assertEquals("com.example.SkippifiedTest2", sourceSet1Class1.getFullyQualifiedClassName());
        assertEquals(Path.of("sourceset2/SkippifiedTest2.class"), sourceSet1Path.getParent().relativize(sourceSet1Class1.getAbsolutePath()));
    }


    private static SourceSetContainer mockSourceSetContainer(String... sourceSetDirectories) {
        var sourceSetContainer = mock(SourceSetContainer.class);
        var sourceSets = asList(sourceSetDirectories).stream().map(ClassFileCollectorTest::mockSourceSet).toList();
        for (int i = 0; i < sourceSets.size(); i++) {
            when(sourceSetContainer.getByName(sourceSetDirectories[i])).thenReturn(sourceSets.get(i));
        }
        when(sourceSetContainer.iterator()).thenReturn(sourceSets.iterator());
        return sourceSetContainer;
    }

    private static SourceSet mockSourceSet(String directory) {
        try {
            File outputDirectory = Paths.get(ClassFileCollectorTest.class.getResource(directory).toURI()).toFile();
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
