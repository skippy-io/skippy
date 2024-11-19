/*
 * Copyright 2023-2024 the original author or authors.
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

package io.skippy.core;

import java.nio.file.Path;
import java.util.List;

/**
 * Data that is being recorded during the execution of a test class:
 * <ul>
 *     <li>the class name (e.g., com.example.FooTest),</li>
 *     <li>the output folder the class is located in (e.g., build/classes/java/test),</li>
 *     <li>a list of {@link TestTag}s,</li>
 *     <li>a list of {@link ClassNameAndJaCoCoId} that represents the classes covered by the test and</li>
 *     <li>the test's JaCoCo execution data</li>
 * </ul>
 *
 * @param className the class name of a test
 * @param outputFolder the output folder the test's class file is located in
 * @param tags a list of {@link TestTag}s
 * @param coveredClasses a list of {@link ClassNameAndJaCoCoId}s
 * @param jacocoExecutionData the test's JaCoCo execution data
 *
 * @author Florian McKee
 */
record TestRecording(String className, Path outputFolder, List<TestTag> tags, List<ClassNameAndJaCoCoId> coveredClasses, byte[] jacocoExecutionData) {
}
