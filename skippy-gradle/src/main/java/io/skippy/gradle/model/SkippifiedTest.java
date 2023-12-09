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

import org.gradle.api.Project;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;

/**
 * Thin wrapper around a skippified test that stores
 * <ul>
 *     <li>the test's {@link ClassFile} and</li>
 *     <li>the name of the test task used to execute the test (e.g., <i>integrationTest</i>).</li>
 * </ul>
 *
 * @author Florian McKee
 */
public class SkippifiedTest {

    private final ClassFile testClassFile;
    private final String testTask;

    /**
     * C'tor
     *
     * @param test
     * @param testTask
     */
    public SkippifiedTest(ClassFile test, String testTask) {
        this.testClassFile = test;
        this.testTask = testTask;
    }

    /**
     * Returns the fully qualified class name (e.g., com.example.FooTest).
     *
     * @return the fully qualified class name (e.g., com.example.FooTest)
     */

    public String getFullyQualifiedClassName() {
        return testClassFile.getFullyQualifiedClassName();
    }

    /**
     * Returns the name of the {@link org.gradle.api.tasks.testing.Test} task to be used to run this test.
     *
     * @return the name of the {@link org.gradle.api.tasks.testing.Test} task to be used to run this test
     */
    public String getTestTask() {
        return testTask;
    }

    /**
     * Returns the relative {@link Path} of the class file relative to the project root,
     * (e.g., src/main/java/com/example/FooTest.java)
     *
     * @return the relative {@link Path} of the class file relative to the project root,
     *      (e.g., src/main/java/com/example/FooTest.java)
     */
    public Path getRelativePath() {
        return testClassFile.getRelativePath();
    }
}