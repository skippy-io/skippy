/*
 * Copyright 2023 the original author or authors.
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

package io.skippy.junit4;

import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * {@link TestRule} that skippifies a JUnit 4 test:
 *
 * <pre>
 * public class FooTest {
 *
 *    {@literal @}ClassRule
 *     public static TestRule skippyRule = Skippy.skippify();
 *
 *    {@literal @}Test
 *     public void testFoo() {
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
 */
public class Skippy extends ExternalResource {

    /**
     * Creates the {@link TestRule} that skippifies a JUnit 4 test.
     *
     * @return the {@link TestRule} that skippifies a JUnit 4 test
     */
    public static TestRule skippify() {
        return RuleChain
                .outerRule(new SkipOrExecuteRule())
                .around(new CoverageFileRule());
    }

}