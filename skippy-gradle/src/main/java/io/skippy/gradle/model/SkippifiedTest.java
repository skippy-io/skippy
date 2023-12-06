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

import java.nio.file.Path;

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

    public SkippifiedTest(ClassFile test, String testTask) {
        this.testClassFile = test;
        this.testTask = testTask;
    }

    public String getFullyQualifiedClassName() {
        return testClassFile.getFullyQualifiedClassName();
    }

    public Path getAbsolutePath() {
        return testClassFile.getAbsolutePath();
    }

    public String getTestTask() {
        return testTask;
    }

}
