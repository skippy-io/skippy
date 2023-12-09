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
 * @author Florian McKee
 */
final class CoverageBuildArguments {

    private final List<String> tasks;
    private final List<String> arguments;

    private CoverageBuildArguments(List<String> tasks, List<String> arguments) {
        this.tasks = tasks;
        this.arguments = arguments;
    }

    static CoverageBuildArguments forSkippifiedTest(SkippifiedTest skippifiedTest) {
        var tasks = asList(
            skippifiedTest.getTestTask(),
            "jacocoTestReport"
        );
        var arguments = asList(
            "-PskippyCoverageBuild=true",
            "-PskippyClassFile=" + skippifiedTest.getRelativePath(),
            "-PskippyTestTask=" + skippifiedTest.getTestTask()
        );
        return new CoverageBuildArguments(tasks, arguments);
    }

    List<String> getTasks() {
        return tasks;
    }

    List<String> getArguments() {
        return arguments;
    }

}