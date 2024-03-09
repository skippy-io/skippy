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

import io.skippy.common.util.ClassNameExtractor;
import io.skippy.common.util.Profiler;

import java.nio.file.Path;
import java.util.*;

import static io.skippy.common.util.HashUtil.debugAgnosticHash;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;

/**
 * Programmatic representation of a .coverage file:
 *
 * <pre>
 *  {
 *     "coveredClasses": [
 * 		    "com/example.Foo",
 * 		    "com/example.Bar",
 *     ],
 *     "executionData": null
 * }
 * </pre>
 *
 * @author Florian McKee
 */
public final class CoverageFile {

    private final List<String> coveredClasses;
    private final byte[] jacocoExecutionData;

    public CoverageFile(List<String> coveredClasses, byte[] jacocoExecutionData) {
        this.coveredClasses = coveredClasses;
        this.jacocoExecutionData = jacocoExecutionData;
    }

    static CoverageFile parse(Tokenizer tokenizer) {
        return null;
    }

    /**
     * Renders this instance as JSON string.
     *
     * @return this instance as JSON string
     */
    public String toJson() {
        return null;
    }

}