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

package io.skippy.junit5;

import org.jacoco.agent.rt.IAgent;
import org.jacoco.agent.rt.RT;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.SessionInfoStore;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstancePreConstructCallback;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;

/**
 * Generates a .cov file in the skippy folder with coverage data for skippified tests if the system property
 * {@code skippyEmitCovFiles} is set.
 *
 * @author Florian McKee
 */
final class SkippyCoverageFileGenerator implements TestInstancePreConstructCallback, TestInstancePreDestroyCallback {

    @Override
    public void preConstructTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext context) {
        // this property is set by Skippy's Gradle plugin whenever a build requests the skippyAnalyze task
        if ( ! Boolean.valueOf(System.getenv().get("skippyEmitCovFiles"))) {
            return;
        }
        IAgent agent = RT.getAgent();
        agent.reset();
    }

    @Override
    public void preDestroyTestInstance(ExtensionContext context) {
        // this property is set by Skippy's Gradle plugin whenever a build requests the skippyAnalyze task
        if ( ! Boolean.valueOf(System.getenv().get("skippyEmitCovFiles"))) {
            return;
        }
        IAgent agent = RT.getAgent();
        emitCovFile(context, agent);
    }

    private static void emitCovFile(ExtensionContext context, IAgent agent) {
        var coveredClasses = new LinkedList<String>();
        byte[] executionData = agent.getExecutionData(true);
        ExecutionDataReader executionDataReader = new ExecutionDataReader(new ByteArrayInputStream(executionData));
        executionDataReader.setSessionInfoVisitor(new SessionInfoStore());
        executionDataReader.setExecutionDataVisitor(new IExecutionDataVisitor() {
            @Override
            public void visitClassExecution(ExecutionData executionData) {
                coveredClasses.add(executionData.getName());
            }
        });
        try {
            executionDataReader.read();
            var name = context.getTestInstance().get().getClass().getName();
            Files.write(Path.of("skippy/%s.cov".formatted(name)), coveredClasses, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.write(Path.of("skippy/%s.exec".formatted(name)), executionData,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write execution data: %s".formatted(e.getMessage()), e);
        }
    }

}