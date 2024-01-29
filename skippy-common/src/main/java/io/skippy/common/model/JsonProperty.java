package io.skippy.common.model;

import java.util.function.Function;

public enum JsonProperty {
    CLASS_NAME("class", ClassFile::getClassName),
    CLASS_FILE("path", ClassFile::getClassFile),
    OUTPUT_FOLDER("outputFolder", ClassFile::getOutputFolder),
    HASH("hash", ClassFile::getHash);

    final String propertyName;
    final Function<ClassFile, Object> propertyValueProvider;

    JsonProperty(String propertyName, Function<ClassFile, Object> propertyValueProvider) {
        this.propertyName = propertyName;
        this.propertyValueProvider = propertyValueProvider;
    }
}
