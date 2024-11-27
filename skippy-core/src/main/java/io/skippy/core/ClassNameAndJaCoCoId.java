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

/**
 * Coverage data extracted from a JaCoCo execution data file.
 *
 * @author Florian McKee
 *
 * @param className the name of the covered class
 * @param jaCoCoId the JaCoCo class id of the covered class
 */
record ClassNameAndJaCoCoId(String className, long jaCoCoId) implements Comparable<ClassNameAndJaCoCoId> {

    static ClassNameAndJaCoCoId from(Class<?> clazz) {
        String resourcePath = clazz.getName().replace('.', '/') + ".class";
        try (var classFileStream = clazz.getClassLoader().getResourceAsStream(resourcePath)) {
            return new ClassNameAndJaCoCoId(clazz.getName(), CRC64.classId(classFileStream.readAllBytes()));
        } catch (IOException e) {
            throw new RuntimeException("Unable to create new instance from %s: %s".formatted(clazz, e), e);
        }
    }

    @Override
    public int compareTo(ClassNameAndJaCoCoId other) {
        if (false == this.className.equals(other.className)) {
            return className.compareTo(other.className());
        }
        return Long.compare(this.jaCoCoId, other.jaCoCoId);
    }
}
