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

import io.skippy.common.util.Profiler;

import java.util.*;
import java.util.stream.IntStream;

import static io.skippy.common.model.ClassFile.JsonProperty.allClassProperties;
import static java.lang.System.lineSeparator;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toMap;

/**
 * Container for {@link ClassFile}s that supports a variety of queries.
 *
 * @author Florian McKee
 */
public class ClassFileContainer {

    private final Set<ClassFile> classFiles = new TreeSet<>();
    private final Map<String, List<ClassFile>> classFilesByClassName = new HashMap<>();
    private final Map<ClassFile, String> idsByClassFile = new HashMap<>();
    private final Map<String, List<String>> idsByClassName = new HashMap<>();

    private final Map<String, ClassFile> classFilesById = new HashMap<>();

    /**
     * Creates a new instance for the given {@code classFiles}.
     * @param classFiles a list of {@link ClassFile}s
     * @return a new instance for the given {@code classFiles}
     */
    public static ClassFileContainer from(List<ClassFile> classFiles) {
        var sorted = classFiles.stream().sorted().toList();
        return new ClassFileContainer(IntStream.range(0, classFiles.size()).boxed()
                .collect(toMap(i -> Integer.toString(i), i -> sorted.get(i))));
    }

     private ClassFileContainer(Map<String, ClassFile> classFilesById) {
        for (var entry : classFilesById.entrySet()) {
            var id = entry.getKey();
            var classFile = entry.getValue();
            classFiles.add(classFile);
            idsByClassFile.put(classFile, id);
            this.classFilesById.put(entry.getKey(), entry.getValue());
            if (false == idsByClassName.containsKey(classFile.getClassName())) {
                idsByClassName.put(classFile.getClassName(), new ArrayList<>());
            }
            idsByClassName.get(classFile.getClassName()).add(id);
            if (false == classFilesByClassName.containsKey(classFile.getClassName())) {
                classFilesByClassName.put(classFile.getClassName(), new ArrayList<>());
            }
            classFilesByClassName.get(classFile.getClassName()).add(classFile);
        }
    }

    ClassFile getById(String id) {
        return classFilesById.get(id);
    }

    /**
     * Returns the ids of the classes that match the provided class name.
     *
     * @param className a class name
     * @return the ids of the classes that match the provided class name
     */
    public List<String> getIdsByClassName(String className) {
        if (false == idsByClassName.containsKey(className)) {
            return emptyList();
        }
        return idsByClassName.get(className);
    }

    String toJson() {
        return toJson(allClassProperties());
    }

    String toJson(ClassFile.JsonProperty... propertiesToRender) {
        StringBuilder result = new StringBuilder();
        var classFilesAsList = new ArrayList<>(classFiles);
        result.append("{" + lineSeparator());
        for (int i = 0; i < classFiles.size(); i++) {
            result.append("\t\t\"%s\": ".formatted(idsByClassFile.get(classFilesAsList.get(i))));
            result.append(classFilesAsList.get(i).toJson(propertiesToRender));
            if (i < classFiles.size() - 1) {
                result.append("," + lineSeparator());
            }
        }
        result.append(lineSeparator());
        result.append("\t}");
        return result.toString();
    }

    static ClassFileContainer parse(Tokenizer tokenizer) {
        return Profiler.profile("ClassFileContainer#parse", () -> {
            tokenizer.skip('{');
            var classFiles = new HashMap<String, ClassFile>();
            while (!tokenizer.peek('}')) {
                var id = tokenizer.next();
                tokenizer.skip(':');
                classFiles.put(id, ClassFile.parse(tokenizer));
                tokenizer.skipIfNext(',');
            }
            tokenizer.skip('}');
            return new ClassFileContainer(classFiles);
        });
    }

    Set<ClassFile> getClassFiles() {
        return unmodifiableSet(classFiles);
    }

    /**
     * Combines the {@link ClassFile}s in this instance with the {@link ClassFile}s in the {@code other} instance.
     *
     * @param other the other instance
     * @return a new {@link ClassFileContainer} that combines the {@link ClassFile}s in this instance with the {@link ClassFile}s in the {@code other} instance
     */
    ClassFileContainer merge(ClassFileContainer other) {
        var result = new HashSet<ClassFile>();
        result.addAll(other.classFiles);
        for (var classFile : this.classFiles) {
            if (false == result.contains(classFile)) {
                result.add(classFile);
            }
        }
        return ClassFileContainer.from(new ArrayList<>(result));
    }

    /**
     * Returns the id of the {@link ClassFile}. The id is used to for referencing purposes in the JSON representation of
     * a {@link TestImpactAnalysis}.
     *
     * @param classFile a {@link ClassFile}
     * @return the id of the {@link ClassFile}
     */
    String getId(ClassFile classFile) {
        return idsByClassFile.get(classFile);
    }
}
