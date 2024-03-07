package io.skippy.common.util;

import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HashUtilTest {

    @Test
    void testDebugAgnosticHashOriginalClass() throws URISyntaxException {
        var classFile = Path.of(getClass().getResource("StringUtils.class").toURI());
        assertEquals("ECE5D94D", HashUtil.debugAgnosticHash(classFile));
    }

    @Test
    void testDebugAgnosticHashOriginalClassWithNewComment() throws URISyntaxException {
        var classFile = Path.of(getClass().getResource("StringUtilsWithComment.class").toURI());
        assertEquals("ECE5D94D", HashUtil.debugAgnosticHash(classFile));
    }

    @Test
    void testDebugAgnosticHashOriginalClassWithNewAnnotation() throws URISyntaxException {
        var classFile = Path.of(getClass().getResource("StringUtilsWithAnnotation.class").toURI());
        assertEquals("17D14E41", HashUtil.debugAgnosticHash(classFile));
    }

}
