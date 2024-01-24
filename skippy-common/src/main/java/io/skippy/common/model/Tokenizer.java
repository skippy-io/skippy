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

package io.skippy.common.model;

import static java.lang.Character.isWhitespace;

/**
 * Home-grown JSON tokenization to avoid a transitive dependencies to Jackson (or some other JSON library).
 *
 * @author Florian McKee
 */
final class Tokenizer {

    private final StringBuilder stream;

    Tokenizer(String input) {
        this.stream = new StringBuilder(input);
    }

    String asString() {
        return stream.toString();
    }

    void skip(String s) {
        skipLeadingWhitespaces();
        if (! asString().startsWith(s)) {
            throw new IllegalStateException("Can't skip over '%s' in residual characters '%s'.".formatted(s, stream));
        }
        stream.delete(0, s.length());
    }

    private void skipLeadingWhitespaces() {
        while (stream.length() > 0 && isWhitespace(stream.charAt(0))) {
            stream.delete(0, 1);
        }
    }

    String next() {
        skipLeadingWhitespaces();
        if (peek("{")) {
            skip("{");
            return "{";
        }
        if (peek("\"")) {
            StringBuilder result = new StringBuilder();
            // skip over opening quotes
            skip("\"");

            // read until closing quotes
            while (peek("\"") == false) {
                result.append(stream.substring(0, 1));
                stream.delete(0, 1);
            }
            // skip over closing quotes
            skip("\"");
            return result.toString();
        }
        throw new IllegalStateException("Unable to determine next oken in residual characters '%s'.".formatted(stream));
    }

    boolean peek(String s) {
        skipLeadingWhitespaces();
        return asString().startsWith(s);
    }
}
