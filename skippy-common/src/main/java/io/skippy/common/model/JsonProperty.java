package io.skippy.common.model;

import java.util.function.Function;

/**
 * Allows tests to specify a sub-set of all properties when rendering a {@link TestImpactAnalysis} to JSON. This makes
 * them less brittle to unrelated changes.
 */
public enum JsonProperty {
    /**
     * The class name.
     */
    CLASS_NAME("name", ClassFile::getClassName),

    /**
     * The path to the class file relative to the output folder.
     */
    CLASS_FILE("path", ClassFile::getClassFile),

    /**
     * The path of the output folder relative to the project root.
     */
    OUTPUT_FOLDER("outputFolder", ClassFile::getOutputFolder),

    /**
     * The hash of the class file.
     */
    HASH("hash", ClassFile::getHash);

    final String propertyName;
    final Function<ClassFile, Object> propertyValueProvider;

    JsonProperty(String propertyName, Function<ClassFile, Object> propertyValueProvider) {
        this.propertyName = propertyName;
        this.propertyValueProvider = propertyValueProvider;
    }
}
