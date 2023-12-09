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

package io.skippy.gradle;

import io.skippy.gradle.model.ClassFile;
import io.skippy.gradle.model.SkippifiedTest;
import org.gradle.api.Project;

import java.nio.file.Path;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Static utility methods that perform the mapping between a {@link SkippifiedTest} and the corresponding coverage build.
 * {@code ./gradlew skippyAnalyze} creates a coverage build per {@link SkippifiedTest} to capture the test coverage for
 * each {@link SkippifiedTest}.
 *
 * @author Florian McKee
 */
final class CoverageBuild {

    /**
     * The Gradle tasks for the coverage build.
     * @return
     */
    static List<String> getTasks(SkippifiedTest skippifiedTest) {
        return asList(
            skippifiedTest.getTestTask(),
            "jacocoTestReport"
        );
    }

    static List<String> getArguments(SkippifiedTest skippifiedTest) {
        return asList(
            "-PskippyCoverageBuild=true",
            "-PskippyClassFile=" + skippifiedTest.getRelativePath(),
            "-PskippyTestTask=" + skippifiedTest.getTestTask()
        );
    }

    /**
     * Returns {@code true} if the {@project} represents a coverage build for a skippified test, {@code false} otherwise.
     *
     * @param project
     * @return {@code true} if the {@project} represents a coverage build for a skippified test, {@code false} otherwise
     */
    static boolean isCoverageBuildForSkippifiedTest(Project project) {
        return project.hasProperty("skippyCoverageBuild");
    }

    /**
     * Returns the {@link SkippifiedTest} for a {@project} that represents a coverage build.
     *
     * @param project
     * @return the {@link SkippifiedTest} for a {@project} that represents a coverage build
     */
    static SkippifiedTest getSkippifiedTest(Project project) {
        var classFile = Path.of(String.valueOf(project.property("skippyClassFile")));
        var testTaskName = String.valueOf(project.property("skippyTestTask"));
        project.getLogger().lifecycle(classFile.toString());
        return new SkippifiedTest(new ClassFile(project, classFile), testTaskName);
    }
}
