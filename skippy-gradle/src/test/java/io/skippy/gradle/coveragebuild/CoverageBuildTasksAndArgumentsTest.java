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

package io.skippy.gradle.coveragebuild;

import io.skippy.gradle.model.ClassFile;
import io.skippy.gradle.model.SkippifiedTest;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link CoverageBuildTasksAndArguments}.
 *
 * @author Florian McKee
 */
public class CoverageBuildTasksAndArgumentsTest {

    @Test
    void testForSkippifiedTest() throws URISyntaxException {
        var classFile = Paths.get(getClass().getResource("LeftPadderTest.class").toURI());
        var project = mock(Project.class);
        when(project.getProjectDir()).thenReturn(classFile.toFile().getParentFile().getParentFile());
        var skippifiedTest = new SkippifiedTest(new ClassFile(project, classFile), "test");
        var tasksAndArgs = CoverageBuildTasksAndArguments.forSkippifiedTest(skippifiedTest);
        assertThat(tasksAndArgs.tasks()).contains(
                "test",
                "jacocoTestReport"
        );
        assertThat(tasksAndArgs.arguments()).contains(
                "-PskippyCoverageBuild=true",
                "-PskippyClassFile=coveragebuild/LeftPadderTest.class",
                "-PskippyTestTask=test"
        );
    }
}
