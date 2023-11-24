package io.skippy.core.model;

/**
 * Allows for more meaningful typing, e.g., <code>Map&lt;FullyQualifiedClassName, List&lt;FullyQualifiedClassName&gt;&gt;</code>
 * instead of <code>Map&lt;String, List&lt;String&gt;&gt;</code>.
 */
public record FullyQualifiedClassName(String fullyQualifiedClassName) {}