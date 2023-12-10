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

import io.skippy.gradle.SkippyPluginExtension;
import io.skippy.gradle.model.SourceSetWithTestTask;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.skippy.gradle.collector.CollectorTestUtils.mockSourceSetContainer;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SkippifiedTestCollector}.
 *
 * @author Florian McKee
 */
public class SkippifiedTestCollectorTest {

    @Test
    void testCollect() throws URISyntaxException {
        var currentDir = Paths.get(getClass().getResource("sourceset1").toURI()).toFile();
        var project = mock(Project.class);
        when(project.getProjectDir()).thenReturn(currentDir.getParentFile().getParentFile());

        var sourceSetContainer = mockSourceSetContainer("sourceset1", "sourceset2", "sourceset3");
        var classFileCollector = new ClassFileCollector(project, sourceSetContainer);
        var skippyPluginExtension = mock(SkippyPluginExtension.class);

        when(skippyPluginExtension.getSourceSetsWithTestTasks()).thenReturn(asList(
                new SourceSetWithTestTask("sourceset1", "test"),
                new SourceSetWithTestTask("sourceset2", "integrationTest"),
                new SourceSetWithTestTask("sourceset3", "functionalTest")
        ));
        var skippifiedTestCollector = new SkippifiedTestCollector(classFileCollector, sourceSetContainer, skippyPluginExtension);

        var skippifiedTests = skippifiedTestCollector.collect();

        assertEquals(3, skippifiedTests.size());

        var skippifiedTests0 = skippifiedTests.get(0);
        assertEquals("com.example.SkippifiedTest1", skippifiedTests0.classFile().getFullyQualifiedClassName());
        assertEquals(Path.of("collector/sourceset1/SkippifiedTest1.class"), skippifiedTests0.classFile().getRelativePath());
        assertEquals("test", skippifiedTests0.testTask());

        var skippifiedTests1 = skippifiedTests.get(1);
        assertEquals("com.example.SkippifiedTest2", skippifiedTests1.classFile().getFullyQualifiedClassName());
        assertEquals(Path.of("collector/sourceset2/SkippifiedTest2.class"), skippifiedTests1.classFile().getRelativePath());
        assertEquals("integrationTest", skippifiedTests1.testTask());

        var skippifiedTests2 = skippifiedTests.get(2);
        assertEquals("com.example.SkippifiedTest3", skippifiedTests2.classFile().getFullyQualifiedClassName());
        assertEquals(Path.of("collector/sourceset3/SkippifiedTest3.class"), skippifiedTests2.classFile().getRelativePath());
        assertEquals("functionalTest", skippifiedTests2.testTask());
    }

}
