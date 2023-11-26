package io.skippy.core;

/**
 * Allows for more meaningful typing, e.g., {@code Map<FullyQualifiedClassName, List<FullyQualifiedClassName>>}
 * instead of {@code Map<String, List<String>>}.
 */
public record FullyQualifiedClassName(String fullyQualifiedClassName) {}