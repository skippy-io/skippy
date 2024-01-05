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

import java.util.logging.Logger;

/**
 * Swallows certain JaCoCo related exceptions to prevent builds being broken by Skippy.
 *
 * @author Florian McKee
 */
class JaCoCoExceptionHandler {

    private static final Logger LOGGER = Logger.getLogger(JaCoCoExceptionHandler.class.getName());

    /**
     * Executes the {@code runnable} and swallows certain JaCoCo related exceptions to prevent builds being broken by
     * Skippy.
     *
     * @param runnable a {@link Runnable}
     */
    static void swallowJaCoCoExceptions(Runnable runnable) {
        try {
            runnable.run();
        } catch (NoClassDefFoundError e) {
            if (e.getMessage().startsWith("org/jacoco")) {
                LOGGER.severe("Unable to load JaCoCo class %s".formatted(e.getMessage()));
                LOGGER.severe("");
                LOGGER.severe("Did you forget to add the JaCoCo plugin to your pom.xml?");

                // suppress exception to continue the build

            } else {
                throw e;
            }
        } catch (IllegalStateException e) {
            if (e.getMessage().equals("JaCoCo agent not started.")) {
                LOGGER.severe("JaCoCo agent unavailable: %s".formatted(e.getMessage()));
                LOGGER.severe("");
                LOGGER.severe("Did you forget to add the JaCoCo plugin to your pom.xml?");

                // suppress exception to continue the build

            } else {
                throw e;
            }
        }
    }

}
