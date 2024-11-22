package io.skippy.core;

import org.jacoco.core.internal.data.CRC64;

import java.io.IOException;

/**
 * Coverage data extracted from a JaCoCo execution data file.
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
