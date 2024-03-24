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
import java.nio.file.Paths;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
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
    void testCollect() throws URISyntaxException {

        var sourceSetContainer = mockSourceSetContainer("main", "test");

        var projectDir = Paths.get(GradleClassFileCollectorTest.class.getResource("build").toURI()).getParent();

        var classFileCollector = new GradleClassFileCollector(projectDir, sourceSetContainer);

        var classFiles = classFileCollector.collect();

        assertEquals(6, classFiles.size());


        assertThat(classFiles.get(0).toJson()).isEqualToIgnoringWhitespace("""
            {
                "name": "com.example.LeftPadder",
                "path": "com/example/LeftPadder.class",
                "outputFolder": "build/classes/java/main",
                "hash": "8E994DD8"
            }
        """);

        assertThat(classFiles.get(1).toJson()).isEqualToIgnoringWhitespace("""
            {
                "name": "com.example.RightPadder",
                "path": "com/example/RightPadder.class",
                "outputFolder": "build/classes/java/main",
                "hash": "F7F27006"
            }
        """);

        assertThat(classFiles.get(2).toJson()).isEqualToIgnoringWhitespace("""
            {
                "name": "com.example.StringUtils",
                "path": "com/example/StringUtils.class",
                "outputFolder": "build/classes/java/main",
                "hash": "ECE5D94D"
            }
        """);

        assertThat(classFiles.get(3).toJson()).isEqualToIgnoringWhitespace("""
            {
                "name": "com.example.LeftPadderTest",
                "path": "com/example/LeftPadderTest.class",
                "outputFolder": "build/classes/java/test",
                "hash": "83A72152"
            }
        """);

        assertThat(classFiles.get(4).toJson()).isEqualToIgnoringWhitespace("""
            {
                "name": "com.example.RightPadderTest",
                "path": "com/example/RightPadderTest.class",
                "outputFolder": "build/classes/java/test",
                "hash": "E5FB1274"
            }
        """);

        assertThat(classFiles.get(5).toJson()).isEqualToIgnoringWhitespace("""
            {
                "name": "com.example.TestConstants",
                "path": "com/example/TestConstants.class",
                "outputFolder": "build/classes/java/test",
                "hash": "119F463C"
            }
        """);
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
            File outputDirectory = Paths.get(GradleClassFileCollectorTest.class.getResource("build/classes/java/" + directory).toURI()).toFile();
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
