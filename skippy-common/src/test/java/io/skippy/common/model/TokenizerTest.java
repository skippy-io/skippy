package io.skippy.common.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TokenizerTest {

    @ParameterizedTest
    @CsvSource(value = {
            "ab",
            "    ab"
    }, delimiter = ':')
    void testSkip(String stream) {
        var tokenizer = new Tokenizer(stream);
        tokenizer.skip("a");
        assertEquals("b", tokenizer.asString());
    }

    @Test
    void testInvalidSkip() {
        var tokenizer = new Tokenizer("a");
        var ex = assertThrows(IllegalStateException.class, () -> tokenizer.skip("b"));
        assertEquals("Can't skip over 'b' in residual characters 'a'.", ex.getMessage());
    }

    @Test
    void testSkipConsumesLeadingWhitespaces() {
        var tokenizer = new Tokenizer("   ab");
        tokenizer.skip("a");
        assertEquals("b", tokenizer.asString());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "{:{",
            "\"value\":value"
    }, delimiter = ':')
    void testNext(String stream, String expectedNextToken) {
        var tokenizer = new Tokenizer(stream);
        assertEquals(expectedNextToken, tokenizer.next());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "ab:a:true",
            "ab:b:false",
            "    ab:a:true",
            "    ab:b:false"
    }, delimiter = ':')
    void testPeek(String stream, String search, boolean expected) {
        var tokenizer = new Tokenizer(stream);
        assertEquals(expected, tokenizer.peek(search));
    }

}
