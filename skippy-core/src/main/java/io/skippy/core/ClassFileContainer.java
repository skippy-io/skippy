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

import java.util.*;
import java.util.stream.IntStream;

import static java.lang.System.lineSeparator;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toMap;

/**
 * Container for {@link ClassFile}s that stores static information about classes in a project.
 * <br /><br />
 * JSON example:
 * <pre>
 * {
 *      "0": {
 *          "name": "com.example.Bar",
 *          "path": "com/example/Bar.class",
 *          "outputFolder": "build/classes/java/main",
 *          "hash": "08B76AA9"
 *      },
 *      "1": {
 *          "name": "com.example.BarTest",
 *          "path": "com/example/BarTest.class",
 *          "outputFolder": "build/classes/java/test",
 *          "hash": "119F463C"
 *      },
 *      ...
 * }
 * </pre>
 *
 * A {@link ClassFileContainer} assigns a numerical id to each analyzed class file. Those ids are used to
 * cross-reference class files within a {@link TestImpactAnalysis}.
 * <br /><br />
 * See {@link TestImpactAnalysis} for an overview how {@link ClassFileContainer} fits into Skippy's data model.
 *
 * @author Florian McKee
 */
final class ClassFileContainer {
    private final Set<ClassFile> classFiles = new TreeSet<>();
    private final Map<String, List<ClassFile>> classFilesByClassName = new HashMap<>();
    private final Map<ClassFile, Integer> idsByClassFile = new HashMap<>();
    private final Map<String, List<Integer>> idsByClassName = new HashMap<>();

    private final Map<Integer, ClassFile> classFilesById = new HashMap<>();

     private ClassFileContainer(Map<Integer, ClassFile> classFilesById) {
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

    /**
     * Creates a new instance for the given {@code classFiles}.
     *
     * @param classFiles a list of {@link ClassFile}s
     * @return a new instance for the given {@code classFiles}
     */
    static ClassFileContainer from(List<ClassFile> classFiles) {
        var sorted = classFiles.stream().sorted().toList();
        return new ClassFileContainer(IntStream.range(0, classFiles.size()).boxed()
                .collect(toMap(i -> i, i -> sorted.get(i))));
    }

    /**
     * Returns the ids of the classes that match the provided class name.
     * <br /><br />
     * Note that class names might not be unique within a project:
     * <pre>
     *     src/integration-test/java/com.example.FooTest
     *     src/functional-test/java/com.example.FooTest
     * </pre>
     * That's why this method returns a list.
     *
     * @param className a class name
     * @return the ids of the classes that match the provided class name
     */
    List<Integer> getIdsByClassName(String className) {
        if (false == idsByClassName.containsKey(className)) {
            return emptyList();
        }
        return idsByClassName.get(className);
    }

    Set<ClassFile> getClassFiles() {
        return unmodifiableSet(classFiles);
    }

    /**
     * Returns the id for the given {@link ClassFile}.
     *
     * @param classFile a {@link ClassFile}
     * @return the id for the given {@link ClassFile}
     */
    int getId(ClassFile classFile) {
        return idsByClassFile.get(classFile);
    }


    /**
     * Returns the {@link ClassFile} with the given id.
     *
     * @param id an id
     * @return the {@link ClassFile} with the given id
     */
    ClassFile getById(int id) {
        return classFilesById.get(id);
    }

    String toJson() {
        StringBuilder result = new StringBuilder();
        result.append("{" + lineSeparator());
        for (int i = 0; i < classFiles.size(); i++) {
            result.append("\t\t\"%s\": ".formatted(i));
            result.append(classFilesById.get(i).toJson());
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
            var classFiles = new HashMap<Integer, ClassFile>();
            while (!tokenizer.peek('}')) {
                var id = Integer.valueOf(tokenizer.next());
                tokenizer.skip(':');
                classFiles.put(id, ClassFile.parse(tokenizer));
                tokenizer.skipIfNext(',');
            }
            tokenizer.skip('}');
            return new ClassFileContainer(classFiles);
        });
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassFileContainer that = (ClassFileContainer) o;
        return Objects.equals(classFilesById, that.classFilesById);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classFilesById);
    }
}
