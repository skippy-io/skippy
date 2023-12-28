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

package io.skippy.junit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Performs helpful logging for exceptions with known root causes.
 *
 * @author Florian McKee
 */
public class SkippyExceptionHandler {

    private static final Logger LOGGER = LogManager.getLogger(SkippyExceptionHandler.class);

    /**
     * Executes the {@code runnable} and performs helpful logging for exceptions with known root cause
     *
     * @param runnable a {@link Runnable}
     */
    public static void executeAndHandleKnownExceptions(Runnable runnable) {
        try {
            runnable.run();
        } catch (NoClassDefFoundError e) {
            if (e.getMessage().startsWith("org/jacoco")) {
                LOGGER.error("Unable to load JaCoCo class %s".formatted(e.getMessage()));
                LOGGER.error("");
                LOGGER.error("Did you forget to add the JaCoCo plugin to your pom.xml?");
            }
            throw e;
        }
    }

}
