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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TokenizerTest {

    @ParameterizedTest
    @CsvSource(value = {
            "[{",
            "    [{"
    }, delimiter = ':')
    void testSkip(String stream) {
        var tokenizer = new Tokenizer(stream);
        tokenizer.skip('[');
        assertEquals("{", tokenizer.asString());
    }

    @Test
    void testInvalidSkip() {
        var tokenizer = new Tokenizer("a");
        var ex = assertThrows(IllegalStateException.class, () -> tokenizer.skip('b'));
        assertEquals("Can't skip over 'b' in residual characters 'a'.", ex.getMessage());
    }

    @Test
    void testSkipConsumesLeadingWhitespaces() {
        var tokenizer = new Tokenizer("  [ foo");
        tokenizer.skip('[');
        assertEquals(" foo", tokenizer.asString());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "{:{",
            "\"value\":value",
            "123:123"
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
    void testPeek(String stream, char search, boolean expected) {
        var tokenizer = new Tokenizer(stream);
        assertEquals(expected, tokenizer.peek(search));
    }

    @Test
    void testTokenWithWhitespace() {
        var tokenizer = new Tokenizer("""
            "name": "foo bar"
        """);
        assertEquals("name", tokenizer.next());
        tokenizer.skip(':');
        assertEquals("foo bar", tokenizer.next());
    }

}
