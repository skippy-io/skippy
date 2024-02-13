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
import io.skippy.common.util.DebugAgnosticHash;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;

/**
 * Programmatic representation of a class file in `test-impact-analysis.json`:
 *
 * <pre>
 *  {
 *      "class": "com.example.Foo",
 *      "path": "com/example/Foo.class",
 *      "outputFolder": "build/classes/java/main",
 *      "hash": "ZT0GoiWG8Az5TevH9/JwBg==",
 *  }
 * </pre>
 *
 * @author Florian McKee
 */
public final class ClassFile implements Comparable<ClassFile> {

    private final String className;
    private final Path outputFolder;
    private final Path classFile;
    private final String hash;

    /**
     * C'tor.
     *
     * @param className the fully qualified class name
     * @param outputFolder the path of the output folder relative to the project root (e.g., build/classes/java/main)
     * @param classFile the path of the class file relative to the output folder (e.g., com/example/Foo.class)
     * @param hash a hash of the class file
     */
    private ClassFile(String className, Path outputFolder, Path classFile, String hash) {
        this.className = className;
        this.outputFolder = outputFolder;
        this.classFile = classFile;
        this.hash = hash;
    }

    /**
     * Creates a new instance based off a class file in one of the project's output folders.
     *
     * @param projectDir   the absolute path of the project (e.g., ~/repo)
     * @param outputFolder the absolute path of the output folder (e.g., ~/repo/build/classes/java/main)
     * @param classFile    the absolute path of the class file (e.g., ~/repos/build/classes/java/main/com/example/Foo.class)
     * @return a new instance based off a class file in one of the project's output folders
     */
    public static ClassFile fromFileSystem(Path projectDir, Path outputFolder, Path classFile) {
        return new ClassFile(
            ClassNameExtractor.getFullyQualifiedClassName(classFile),
            projectDir.relativize(outputFolder),
            outputFolder.relativize(classFile),
            classFile.toFile().exists() ? DebugAgnosticHash.hash(classFile) : ""
        );
    }

    /**
     * Creates a new instance based off parsed JSON.
     *
     * @param className    the fully qualified class name
     * @param outputFolder the path of the output folder relative to the project root (e.g., build/classes/java/main)
     * @param classFile    the path of the class file relative to the output folder (e.g., com/example/Foo.class)
     * @param hash         a hash of the class file
     * @return a new instance based off parsed JSON
     */
    public static ClassFile fromParsedJson(String className, Path outputFolder, Path classFile, String hash) {
        return new ClassFile(className, outputFolder, classFile, hash);
    }

    static ClassFile parse(Tokenizer tokenizer) {
        tokenizer.skip('{');
        var entries = new HashMap<String, String>();
        while (entries.size() < 4) {
            var key = tokenizer.next();
            tokenizer.skip(':');
            var value = tokenizer.next();
            entries.put(key, value);
            if (entries.size() < 4) {
                tokenizer.skip(',');
            }
        }
        tokenizer.skip('}');
        var result = fromParsedJson(entries.get("name"), Path.of(entries.get("outputFolder")), Path.of(entries.get("path")), entries.get("hash"));
        return result;
    }

    /**
     * Renders this instance as JSON string.
     *
     * @return the instance as JSON string
     */
    public String toJson() {
        return toJson(JsonProperty.values());
    }

    /**
     * Renders this instance as JSON string. The only difference compared to
     * {@link ClassFile#toTestClassJson(JsonProperty...)} is indentation.
     *
     * @param propertiesToRender the properties that should be rendered (rendering only a sub-set is useful for testing)
     * @return this instance as JSON string
     */
    public String toJson(JsonProperty... propertiesToRender) {
        var result = new StringBuilder();
        result.append("\t\t\t{" + System.lineSeparator());
        var properties = Arrays.stream(propertiesToRender)
                .map(jsonProperty -> "\t\t\t\t\"%s\": \"%s\"".formatted(jsonProperty.propertyName, jsonProperty.propertyValueProvider.apply(this)))
                .collect(joining("," + System.lineSeparator()));
        result.append(properties + System.lineSeparator());
        result.append("\t\t\t}");
        return result.toString();
    }

    /**
     * Renders this instance as JSON string. The only difference compared to {@link ClassFile#toJson(JsonProperty...)}
     * is indentation.
     *
     * @param propertiesToRender the properties that should be rendered (rendering only a sub-set is useful for testing)
     * @return the instance as JSON string
     */
    String toTestClassJson(JsonProperty... propertiesToRender) {
        var result = new StringBuilder();
        result.append("{" + System.lineSeparator());
        var properties = Arrays.stream(propertiesToRender)
                .map(jsonProperty -> "\t\t\t\"%s\": \"%s\"".formatted(jsonProperty.propertyName, jsonProperty.propertyValueProvider.apply(this)))
                .collect(joining("," + System.lineSeparator()));
        result.append(properties + System.lineSeparator());
        result.append("\t\t}");
        return result.toString();
    }

    /**
     * Returns the fully qualified class name (e.g., com.example.Foo).
     *
     * @return the fully qualified class name (e.g., com.example.Foo)
     */
    public String getClassName() {
        return className;
    }

    @Override
    public int compareTo(ClassFile other) {
        return comparing(ClassFile::getClassName)
                .thenComparing(ClassFile::getOutputFolder)
                .compare(this, other);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ClassFile c) {
            return Objects.equals(getClassName() + getOutputFolder(), c.getClassName()  + c.getOutputFolder());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClassName(), getOutputFolder());
    }

    /**
     * Returns the output folder that contains the class (e.g., ~/repo/build/classes/java/main).
     *
     * @return the output folder that contains the class
     */
    Path getOutputFolder() {
        return outputFolder;
    }

    /**
     * Returns the path of the class file relative to the output folder (e.g., com/example/Foo.class).
     *
     * @return the path of the class file relative to the output folder (e.g., com/example/Foo.class)
     */
    Path getClassFile() {
        return classFile;
    }

    /**
     * Returns a hash of the class file.
     *
     * @return a hash of the class file
     */
    String getHash() {
        return hash;
    }

    boolean hasChanged() {
        return ! hash.equals(DebugAgnosticHash.hash(outputFolder.resolve(classFile)));
    }

    boolean classFileNotFound() {
        return ! outputFolder.resolve(classFile).toFile().exists();
    }
}
