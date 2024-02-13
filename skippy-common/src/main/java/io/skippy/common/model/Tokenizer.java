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

/**
 * Home-grown JSON tokenization to avoid a transitive dependencies to Jackson (or some other JSON library).
 *
 * @author Florian McKee
 */
final class Tokenizer {

    private final StringBuilder stream;

    Tokenizer(String input) {
        this.stream = new StringBuilder(input.replaceAll("\\s+",""));
    }

    String asString() {
        return stream.toString();
    }

    void skip(char c) {
            if (stream.isEmpty() || stream.charAt(0) != c) {
                throw new IllegalStateException("Can't skip over '%s' in residual characters '%s'.".formatted(c, stream));
            }
            stream.delete(0, 1);
    }

    String next() {
            if (peek('{')) {
                stream.delete(0, 1);
                return "{";
            }
            if (peek('"')) {
                int positionOfClosingQuote = stream.indexOf("\"", 1);
                var result = stream.substring(1, positionOfClosingQuote);
                stream.delete(0, positionOfClosingQuote + 1);
                return result;
            }
            throw new IllegalStateException("Unable to determine next token in residual characters '%s'.".formatted(stream));
    }


    boolean peek(char c) {
            if (stream.isEmpty()) {
                return false;
            }
            return stream.charAt(0) == c;
    }
    void skipIfNext(char c) {
        if (stream.isEmpty()) {
            return;
        }
        if (stream.charAt(0) == c) {
            stream.delete(0, 1);
        }
    }

    public String getPrefixIncluding(char c) {
        return stream.substring(0, 1 + stream.indexOf(String.valueOf(c)));
    }

    public void skip(int length) {
        stream.delete(0, length);
    }
}