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

import java.util.Arrays;

/**
 * Home-grown JSON tokenization to avoid a transitive dependencies to Jackson (or some other JSON library).
 *
 * @author Florian McKee
 */
final class Tokenizer {

    private final char[] stream;
    private int head;
    private final int tail;

    Tokenizer(String input) {
        this.stream = input.replaceAll("\\s+","").toCharArray();
        this.head = 0;
        this.tail = stream.length;
    }

    @Override
    public String toString() {
        return asString();

    }
    String asString() {
        return new String(Arrays.copyOfRange(stream, head, tail));
    }

    void skip(char c) {
        if (head == tail || stream[head] != c) {
             throw new IllegalStateException("Can't skip over '%s' in residual characters '%s'.".formatted(c, asString()));
        }
        head++;
    }

    String next() {
        if (peek('{')) {
            head++;
            return "{";
        }
        if (peek('"')) {
            int pointer = head + 1;
            while (stream[pointer] != '"') {
                pointer++;
            }
            var result = new String(Arrays.copyOfRange(stream, head + 1, pointer));
            head = pointer + 1;
            return result;
        }
        throw new IllegalStateException("Unable to determine next token in residual characters '%s'.".formatted(asString()));
    }

    boolean peek(char c) {
        if (head == tail) {
            return false;
        }
        return stream[head] == c;
    }
    void skipIfNext(char c) {
        if (head == tail) {
            return;
        }
        if (stream[head] == c) {
            head++;
        }
    }

}