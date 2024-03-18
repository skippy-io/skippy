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

import static io.skippy.common.model.ClassFile.JsonProperty.allClassProperties;
import static io.skippy.common.util.HashUtil.debugAgnosticHash;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.exists;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;

/**
 * Represents a class file that has been analyzed by Skippy.
 * <br /><br />
 * JSON example:
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

    private final String clazz;
    private final Path outputFolder;
    private final Path path;
    private final String hash;

    /**
     * Allows test to specify which properties to include in the JSON representation. This allows tests to focus on a
     * sub-set of all properties instead of asserting against the value of all properties.
     */
    public enum JsonProperty {

        /**
         * The fully qualified class name.
         */
        CF_CLASS,

        /**
         * The path of the class file relative to the output folder (e.g., com/example/Foo.class).
         */
        CF_PATH,

        /**
         * The path of the output folder relative to the project root (e.g., build/classes/java/main).
         */
        CF_OUTPUT_FOLDER,

        /**
         * The hash of the class file.
         */
        CF_HASH;

        /**
         * Convenience method for tests that assert against a sub-set of the JSON representation.
         *
         * @param properties the input
         * @return the input
         */
        public static JsonProperty[] classProperties(ClassFile.JsonProperty... properties) {
            return properties;
        }

        /**
         * Convenience method for tests that assert against the entire JSON representation.
         *
         * @return all properties
         */
        public static JsonProperty[] allClassProperties() {
            return values();
        }
    }

    /**
     * C'tor.
     *
     * @param clazz the fully qualified class name
     * @param outputFolder the path of the output folder relative to the project root (e.g., build/classes/java/main)
     * @param path the path of the class file relative to the output folder (e.g., com/example/Foo.class)
     * @param hash a hash of the class file
     */
    private ClassFile(String clazz, Path outputFolder, Path path, String hash) {
        this.clazz = clazz;
        this.outputFolder = outputFolder;
        this.path = path;
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
            exists(classFile) ? debugAgnosticHash(classFile) : ""
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
        return Profiler.profile("ClassFile#parse", () -> {
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
        });
    }

    /**
     * Renders this instance as JSON string.
     *
     * @return the instance as JSON string
     */
    public String toJson() {
        return toJson(allClassProperties());
    }


    /**
     * Renders this instance as JSON string.
     *
     * @param propertiesToRender the properties that should be rendered (rendering only a sub-set is useful for testing)
     * @return the instance as JSON string
     */
    String toJson(JsonProperty... propertiesToRender) {
        var result = new StringBuilder();
        result.append("{" + lineSeparator());
        var renderedProperties = new ArrayList<String>();
        for (var propertyToRender : propertiesToRender) {
            renderedProperties.add(switch (propertyToRender) {
                case CF_CLASS -> "\t\t\t\"name\": \"%s\"".formatted(clazz);
                case CF_PATH -> "\t\t\t\"path\": \"%s\"".formatted(path);
                case CF_OUTPUT_FOLDER -> "\t\t\t\"outputFolder\": \"%s\"".formatted(outputFolder);
                case CF_HASH -> "\t\t\t\"hash\": \"%s\"".formatted(hash);
            });
        }
        result.append(renderedProperties.stream().collect(joining("," +  lineSeparator())));
        result.append(lineSeparator());
        result.append("\t\t}");
        return result.toString();
    }

    /**
     * Returns the fully qualified class name (e.g., com.example.Foo).
     *
     * @return the fully qualified class name (e.g., com.example.Foo)
     */
    public String getClazz() {
        return clazz;
    }

    @Override
    public int compareTo(ClassFile other) {
        return comparing(ClassFile::getClazz)
                .thenComparing(ClassFile::getOutputFolder)
                .compare(this, other);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ClassFile c) {
            return Objects.equals(getClazz() + getOutputFolder(), c.getClazz()  + c.getOutputFolder());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClazz(), getOutputFolder());
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
    Path getPath() {
        return path;
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
        return ! hash.equals(debugAgnosticHash(outputFolder.resolve(path)));
    }

    boolean classFileNotFound() {
        return false == exists(outputFolder.resolve(path));
    }
}
