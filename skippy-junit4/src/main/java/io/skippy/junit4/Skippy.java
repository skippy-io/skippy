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

package io.skippy.junit4;

import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * {@link TestRule} that enables Skippy's predictive test selection for a JUnit 4 test:
 *
 * <pre>
 * public class FooTest {
 *
 *    {@literal @}ClassRule
 *     public static TestRule skippyRule = Skippy.predictWithSkippy();
 *
 *    {@literal @}Test
 *     public void testFoo() {
 *         ...
 *     }
 *
 * }
 * </pre>
 */
public class Skippy extends ExternalResource {

    /**
     * Creates a {@link TestRule} that enables predictive test selection for a JUnit 4 test.
     *
     * @return a {@link TestRule} that enables predictive test selection for a JUnit 4 test
     */
    public static TestRule predictWithSkippy() {
        return RuleChain
                .outerRule(new SkipOrExecuteRule())
                .around(new CoverageFileRule());
    }

}