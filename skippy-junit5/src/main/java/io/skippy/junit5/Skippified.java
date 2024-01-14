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

package io.skippy.junit5;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Skippifies a JUnit test:
 * <br />
 * <pre>
 * {@literal @}Skippified
 *  public class FooTest {
 *
 *    {@literal @}Test
 *     void testFoo() {
 *         ...
 *     }
 *
 * }
 * </pre>
 *
 * A skippified test differs from a regular JUnit test in two ways:
 * <ul>
 *     <li>It will be skipped if Skippy decides that it is safe to do so.</li>
 *     <li>It will emit a .cov file in the skippy directory when the system property {@code skippyEmitCovFiles} is set.</li>
 * </ul>
 *
 * @author Florian McKee
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(SkipOrExecuteCondition.class)
@ExtendWith(CoverageFileCallbacks.class)
public @interface Skippified {
}