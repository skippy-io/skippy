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

import io.skippy.gradle.model.SkippifiedTest;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Represents the tasks and build arguments used to run a coverage build for a skippified test.
 *
 * @author Florian McKee
 */
final class CoverageBuildTasksAndArguments {

    private final List<String> tasks;
    private final List<String> arguments;

    private CoverageBuildTasksAndArguments(List<String> tasks, List<String> arguments) {
        this.tasks = tasks;
        this.arguments = arguments;
    }

    /**
     * Creates a new instance for a {@link SkippifiedTest}.
     *
     * @param skippifiedTest
     * @return a new instance for a {@link SkippifiedTest}
     */
    static CoverageBuildTasksAndArguments forSkippifiedTest(SkippifiedTest skippifiedTest) {
        var tasks = asList(
            skippifiedTest.getTestTask(),
            "jacocoTestReport"
        );
        var arguments = asList(
            "-PskippyCoverageBuild=true",
            "-PskippyClassFile=" + skippifiedTest.getRelativePath(),
            "-PskippyTestTask=" + skippifiedTest.getTestTask()
        );
        return new CoverageBuildTasksAndArguments(tasks, arguments);
    }

    /**
     * Returns the tasks that need to be executed for the coverage build for the skippified test.
     *
     * @return the tasks that need to be executed for the coverage build for the skippified test
     */
    List<String> getTasks() {
        return tasks;
    }

    /**
     * Returns the arguments that need to be passed to the coverage build for the skippified test.
     *
     * @return the arguments that need to be passed to the coverage build for the skippified test
     */
    List<String> getArguments() {
        return arguments;
    }

}