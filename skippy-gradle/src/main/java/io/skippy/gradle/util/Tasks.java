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

package io.skippy.gradle.util;

import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.testing.Test;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import static io.skippy.gradle.util.SourceSets.findSourceSetContaining;

/**
 * Utility methods that return / operate on to Gradle {@link org.gradle.api.Task}s.
 *
 * @author Florian McKee
 */
public final class Tasks {

    /**
     * Returns the {@link Test} task that runs the {@code testClass}.
     *
     * <br /><br />
     *
     * Let's assume you have the <b>test</b> source set in <code>src/test</code> and another, custom source set
     * <b>intTest</b> for integration tests that is declared as follows:
     * <pre>
     * sourceSets {
     *     intTest {
     *         compileClasspath += sourceSets.main.output
     *         runtimeClasspath += sourceSets.main.output
     *     }
     * }
     * </pre>
     * Additionally, the project registers the task <b>integrationTest</b> to run the tests in <b>intTest</b>:
     * <pre>
     * tasks.register('integrationTest', Test) {
     *     testClassesDirs = sourceSets.intTest.output.classesDirs
     *     classpath = sourceSets.intTest.runtimeClasspath
     * }
     * </pre>
     *
     * This method will
     * <ul>
     *  <li>return <b>test</b> if the test is located in build/classes/java/test</li>
     *  <li>return <b>integrationTest</b> if the test is located in build/classes/java/intTest</li>
     * </ul>
     *
     * @param project the Gradle {@link Project}
     * @param testClass the class file that contains a JUnit test
     * @return the {@link Test} task that runs the {@code testClass}
     */
    public static Test getTestTaskFor(Project project, Path testClass) {
        var sourceSet = findSourceSetContaining(project, testClass);
        var testTaskRef = new AtomicReference<Test>();
        project.getTasks().withType(Test.class, testTask -> {
            if (isTestTaskForSourceSet(testTask, sourceSet)) {
                testTaskRef.set(testTask);
            }
        });
        if (testTaskRef.get() == null) {
            throw new RuntimeException("Unable to determine test task for '%s'.".formatted(testClass));
        }
        return testTaskRef.get();
    }

    private static boolean isTestTaskForSourceSet(Test testTask, SourceSet sourceSet) {
        return sourceSet.getOutput().getClassesDirs().getFiles().containsAll(testTask.getTestClassesDirs().getFiles());
    }

}