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

package io.skippy.common.model;

import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

/**
 * Allows tests to specify a sub-set of all properties when rendering a {@link TestImpactAnalysis} to JSON. This makes
 * them less brittle to unrelated changes.
 *
 * @author Florian McKee
 */
public final class JsonConfiguration {

    public static enum Classes {

        /**
         * The class name.
         */
        NAME("name", ClassFile::getClassName),

        /**
         * The path to the class file relative to the output folder.
         */
        FILE("path", ClassFile::getClassFile),

        /**
         * The path of the output folder relative to the project root.
         */
        OUTPUT_FOLDER("outputFolder", ClassFile::getOutputFolder),

        /**
         * The hash of the class file.
         */
        HASH("hash", ClassFile::getHash);

        final String propertyName;
        final Function<ClassFile, Object> propertyValueProvider;

        Classes(String propertyName, Function<ClassFile, Object> propertyValueProvider) {
            this.propertyName = propertyName;
            this.propertyValueProvider = propertyValueProvider;
        }

        public static List<Classes> all() {
            return asList(values());
        }
    }

    public static enum Tests {

        /**
         * The test's class id .
         */
        CLASS("class", test -> "\"%s\"".formatted(test.testClassId())),

        /**
         * The test result.
         */
        RESULT("result", test -> "\"%s\"".formatted(test.result())),

        /**
         * The ids of the classes covered by this test.
         */
        COVERED_CLASSES("coveredClasses", test -> "[%s]".formatted(test.coveredClassesIds().stream()
                .map(Integer::valueOf)
                .sorted()
                .map(id -> "\"%s\"".formatted(id)).collect(joining(",")))),

        /**
         * The execution data identifier.
         */
        EXECUTION("execution", test -> "\"%s\"".formatted(test.execution()));

        final String propertyName;
        final Function<AnalyzedTest, Object> propertyValueProvider;

        Tests(String propertyName, Function<AnalyzedTest, Object> propertyValueProvider) {
            this.propertyName = propertyName;
            this.propertyValueProvider = propertyValueProvider;
        }

        public static List<Tests> all() {
            return asList(values());
        }
    }

}
