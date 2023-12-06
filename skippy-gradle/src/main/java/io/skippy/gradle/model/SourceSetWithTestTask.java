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

package io.skippy.gradle.model;

import io.skippy.gradle.SkippyPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.testing.Test;

/**
 * A 2-tuple containing
 * <ul>
 *     <li>the name of a {@link SourceSet} and</li>
 *     <li>the name of the corresponding {@link Test} task.</li>
 * </ul>
 * The {@link SkippyPluginExtension} uses this class to store customizations:
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
 */
public class SourceSetWithTestTask {

    private String name;
    private String testTask;

    /**
     * C'tor.
     *
     * @param sourceSetName the name of a {@link SourceSet}
     * @param testTask the name of the {@link Test} task to execute tests in the {@link SourceSet}
     */
    public SourceSetWithTestTask(String sourceSetName, String testTask) {
        this.name = sourceSetName;
        this.testTask = testTask;
    }

    /**
     * Returns the name of a {@link SourceSet}.
     *
     * @return the name of a {@link SourceSet}
     */
    public String getSourceSetName() {
        return name;
    }

    /**
     * Returns the name of the {@link Test} task to execute tests in the {@link SourceSet}.
     *
     * @return the name of the {@link Test} task to execute tests in the {@link SourceSet}
     */
    public String getTestTask() {
        return testTask;
    }

}
