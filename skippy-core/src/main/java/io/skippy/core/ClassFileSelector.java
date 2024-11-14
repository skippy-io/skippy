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

import java.util.List;

/**
 * Strategy to select {@link ClassFile}s from a {@link ClassFileContainer} based on
 *
 * <ul>
 *     <li>class name and </li>
 *     <li>class path.</li>
 * </ul>
 *
 * Why is this needed? The same class name can occur multiple times within a project:
 *
 * <pre>
 *     src/main/java/com.example.Foo
 *     src/test/java/com.example.Foo
 * </pre>
 *
 * The above example will result in multiple entries for the same class name in test-impact-analysis.java:
 *
 * <pre>
 * {
 *     "classes": {
 *         "1": {
 *             "name": "com.example.Foo",
 *             "path": "com/example/Foo.class",
 *             "outputFolder": "build/classes/java/integrationTest",
 *             ...
 *         }
 *         "1": {
 *             "name": "com.example.Foo",
 *             "path": "com/example/Foo.class",
 *             "outputFolder": "build/classes/java/test",
 *             ...
 *         }
 *     ]
 * }
 * </pre>
 *
 * A {@link ClassFileSelector} can try to map a class name to a single {@link ClassFile} by taking
 * the class path into account.
 *
 * <br /><br />
 *
 * Let's consider the classpath to be as follows:
 *
 * <pre>
 * build/classes/java/test
 * build/classes/java/main
 * build/resources/main
 * </pre>
 *
 * In the above classpath, com.example.Foo can be mapped to
 *
 * <pre>
 * {
 *      "name": "com.example.Foo",
 *      "path": "com/example/Foo.class",
 *      "outputFolder": "build/classes/java/test",
 *      ...
 * }
 * </pre>
 *
 * Now, let's consider the following classpath:
 *
 * <pre>
 * build/classes/java/integrationTest
 * build/classes/java/main
 * build/resources/main
 * </pre>
 *
 * In this example, com.example.Foo can be mapped to
 *
 * <pre>
 * {
 *      "name": "com.example.Foo",
 *      "path": "com/example/Foo.class",
 *      "outputFolder": "build/classes/java/integrationTest",
 *      ...
 * }
 * </pre>
 *
 * If no exact match is possible, then multiple {@link ClassFile}s can be returned. This is safe, but it might result in
 * unnecessary {@link Prediction#EXECUTE} predictions.
 *
 * <br /><br />
 *
 * Custom implementations must have a public no-args constructor.
 * They can be registered using Skippy's build plugins.
 *
 * <br /><br />
 *
 * Gradle example:
 * <pre>
 * skippy {
 *     ...
 *     classFileSelector = 'com.example.CustomClassFileSelector'
 * }
 * </pre>
 *
 * @author Florian McKee
 */
public interface ClassFileSelector {

    /**
     * Select {@link ClassFile}s from the {@code classFileContainer} based on
     *
     * <ul>
     *     <li>{@code className} and </li>
     *     <li>{@code classPath}.</li>
     * </ul>
     * @param className a class name (e.g., com.example.Foo)
     * @param candidates all {@link ClassFile}s that match the {@code className}
     * @param classPath a list of folders within the project's output folders that contribute to the classpath (e.g. build/classes/java/main).
     *
     * @return all matching {@link ClassFile}s
     */
    List<ClassFile> select(String className, List<ClassFile> candidates, List<String> classPath);
}
