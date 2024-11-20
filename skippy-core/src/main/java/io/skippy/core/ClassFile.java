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

import org.jacoco.core.internal.data.CRC64;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.lang.System.lineSeparator;
import static java.nio.file.Files.exists;
import static java.util.Comparator.comparing;

/**
 * A class file that has been analyzed by Skippy.
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
 * See {@link TestImpactAnalysis} for an overview how {@link ClassFile} fits into Skippy's data model.
 *
 * @author Florian McKee
 */
public final class ClassFile implements Comparable<ClassFile> {

    private final String className;
    private final Path path;
    private final Path outputFolder;

    // this is needed if the build plugins call a method that needs to access a class file on the file system
    private final Path fullyQualifiedPath;
    private final String hash;

    /**
     * C'tor.
     *
     * @param className             the fully qualified class name
     * @param fullyQualifiedPath    the fully qualified path of the class file
     * @param path                  the path of the class file relative to the output folder (e.g., com/example/Foo.class)
     * @param outputFolder          the path of the output folder relative to the project root (e.g., build/classes/java/main)
     * @param hash                  a hash of the class file
     */
    ClassFile(String className, Path fullyQualifiedPath, Path path, Path outputFolder, String hash) {
        this.className = className;
        this.fullyQualifiedPath = fullyQualifiedPath;
        this.outputFolder = outputFolder;
        this.path = path;
        this.hash = hash;
    }

    /**
     * C'tor.
     *
     * @param className    the fully qualified class name
     * @param path         the path of the class file relative to the output folder (e.g., com/example/Foo.class)
     * @param outputFolder the path of the output folder relative to the project root (e.g., build/classes/java/main)
     * @param hash         a hash of the class file
     */
    ClassFile(String className, Path path, Path outputFolder, String hash) {
        this(className, null, path, outputFolder, hash);
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
                classFile,
                outputFolder.relativize(classFile), projectDir.relativize(outputFolder),
                exists(classFile) ? HashUtil.debugAgnosticHash(classFile) : ""
        );
    }

    /**
     * Returns the fully qualified class name (e.g., com.example.Foo).
     *
     * @return the fully qualified class name (e.g., com.example.Foo)
     */
    String getClassName() {
        return className;
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
     * Returns the hash of the class file.
     *
     * @return the hash of the class file
     */
    String getHash() {
        return hash;
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
            var result = new ClassFile(entries.get("name"), Path.of(entries.get("path")), Path.of(entries.get("outputFolder")), entries.get("hash"));
            return result;
        });
    }


    /**
     * Renders this instance as JSON string.
     *
     * @return the instance as JSON string
     */
    public String toJson() {
        var result = new StringBuilder();
        result.append("{" + lineSeparator());
        result.append("\t\t\t\"name\": \"%s\",".formatted(className));
        result.append(lineSeparator());
        result.append("\t\t\t\"path\": \"%s\",".formatted(path));
        result.append(lineSeparator());
        result.append("\t\t\t\"outputFolder\": \"%s\",".formatted(outputFolder));
        result.append(lineSeparator());
        result.append("\t\t\t\"hash\": \"%s\"".formatted(hash));
        result.append(lineSeparator());
        result.append("\t\t}");
        return result.toString();
    }

    @Override
    public int compareTo(ClassFile other) {
        return comparing(ClassFile::getClassName)
                .thenComparing(ClassFile::getPath)
                .thenComparing(ClassFile::getOutputFolder)
                .compare(this, other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassFile classFile = (ClassFile) o;
        return Objects.equals(className, classFile.className) &&
                Objects.equals(path, classFile.path) &&
                Objects.equals(outputFolder, classFile.outputFolder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, path, outputFolder);
    }

    boolean hasChanged() {
        return ! hash.equals(HashUtil.debugAgnosticHash(outputFolder.resolve(path)));
    }

    boolean classFileNotFound() {
        return false == exists(outputFolder.resolve(path));
    }

    long getJaCoCoId() {
        try {
            return CRC64.classId(Files.readAllBytes(fullyQualifiedPath));
        } catch (IOException e) {
            throw new RuntimeException("Unable to compute JaCoCo id for %s: %s".formatted(fullyQualifiedPath, e), e);
        }
    }

}
