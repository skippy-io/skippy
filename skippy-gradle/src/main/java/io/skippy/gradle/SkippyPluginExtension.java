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

import io.skippy.gradle.model.SourceSetWithTestTask;
import org.gradle.api.Action;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Extension that allows build to customize
 * <ul>
 *     <li>which SourceSet to consider when looking for skippified tests and</li>
 *     <li>which  task to use to execute a skippified test in a given SourceSet.</li>
 * </ul>
 *
 * The defaults are:
 * <ul>
 *      <li>Look for skippified tests in the {@code test} SourceSet</li>
 *      <li>Execute skippifed tests using the {@code test} task</li>
 *  </ul>
 *
 * Builds can configure the extension using the {@code skippy} DSL block:
 * <pre>
 * skippy {
 *     sourceSet {
 *         name = 'test'
 *         testTask = 'test'
 *     }
 *     sourceSet {
 *         name = 'intTest'
 *         testTask = 'integrationTest'
 *     }
 * }
 * </pre>
 *
 * The above example reads as follows:
 * <ul>
 *     <li>Skippy will look for skippified tests in the SourceSets {@code test} and {@code intTest}</li>
 *     <li>Skippified tests in the {@code test} SourceSet will be executed using the {@code test} task</li>
 *     <li>Skippified tests in the {@code intTest} SourceSet will be executed using the {@code integrationTest} task</li>
 * </ul>
 *
 * @author Florian McKee
 */
public class SkippyPluginExtension {

    private List<SourceSetWithTestTask> sourceSetsWithTestTask = new ArrayList<>();

    /**
     * Returns the {@link SourceSetWithTestTask}s that will be considered by Skippy.
     * @return the {@link SourceSetWithTestTask}s that will be considered by Skippy
     */
    public List<SourceSetWithTestTask> getSourceSetsWithTestTasks() {
        if (sourceSetsWithTestTask.isEmpty()) {
            return asList(new SourceSetWithTestTask("test", "test"));
        }
        return sourceSetsWithTestTask;
    }

    public void sourceSet(Action<SourceSetWithTestTask> action) {
        var sourceSetAndTestTask = new SourceSetWithTestTask(null, null);
        sourceSetsWithTestTask.add(sourceSetAndTestTask);
        action.execute(sourceSetAndTestTask);
    }


}
