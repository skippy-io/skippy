package io.skippy.core.asm;

import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DebugAgnosticHashTest {

    @Test
    void testOriginalClass() throws URISyntaxException {
        var classFile = Path.of(getClass().getResource("StringUtils.class").toURI());
        assertEquals("4VP9fWGFUJHKIBG47OXZTQ==", DebugAgnosticHash.hash(classFile));
    }

    @Test
    void testOriginalClassWithNewComment() throws URISyntaxException {
        var classFile = Path.of(getClass().getResource("StringUtilsWithComment.class").toURI());
        assertEquals("4VP9fWGFUJHKIBG47OXZTQ==", DebugAgnosticHash.hash(classFile));
    }

    @Test
    void testOriginalClassWithNewAnnotation() throws URISyntaxException {
        var classFile = Path.of(getClass().getResource("StringUtilsWithAnnotation.class").toURI());
        assertEquals("ZygI2p1Kb3I7WghMF9FOQQ==", DebugAgnosticHash.hash(classFile));
    }

}
