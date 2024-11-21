package io.skippy.core;

import java.net.URISyntaxException;
import java.nio.file.Path;

class PathUtil {

    static Path getRelativePath(Path reference, Class<?> clazz) {
        try {
            return reference.toAbsolutePath().relativize((Path.of(clazz.getProtectionDomain().getCodeSource().getLocation().toURI())));
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to obtain relative path for reference %s and class %s: %s".formatted(reference, clazz, e), e);
        }
    }

}
