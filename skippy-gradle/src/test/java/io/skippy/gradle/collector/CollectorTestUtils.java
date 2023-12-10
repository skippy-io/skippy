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

import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.SourceSetOutput;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility method to mock a {@link SourceSet}.
 *
 * @author Florian McKee
 */
class CollectorTestUtils {

    static SourceSetContainer mockSourceSetContainer(String... sourceSetDirectories) {
        var sourceSetContainer = mock(SourceSetContainer.class);
        var sourceSets = asList(sourceSetDirectories).stream().map(CollectorTestUtils::mockSourceSet).toList();
        for (int i = 0; i < sourceSets.size(); i++) {
            when(sourceSetContainer.getByName(sourceSetDirectories[i])).thenReturn(sourceSets.get(i));
        }
        when(sourceSetContainer.iterator()).thenReturn(sourceSets.iterator());
        return sourceSetContainer;
    }

    private static SourceSet mockSourceSet(String directory) {
        try {
            File outputDirectory = Paths.get(CollectorTestUtils.class.getResource(directory).toURI()).toFile();
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
