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

package io.skippy.gradle.collector;

import io.skippy.gradle.SkippyPluginExtension;
import io.skippy.gradle.asm.SkippyJUnit5Detector;
import io.skippy.gradle.model.SkippifiedTest;
import io.skippy.gradle.model.SourceSetWithTestTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;

import java.util.LinkedList;
import java.util.List;

import static java.util.Comparator.comparing;

/**
 * Collects {@link SkippifiedTest}s in a project.
 *
 * @author Florian McKee
 */
public final class SkippifiedTestCollector  {

    private final Project project;
    private final ClassFileCollector classFileCollector;
    private final SourceSetContainer sourceSetContainer;
    private final SkippyPluginExtension skippyPluginExtension;

    /**
     * C'tor.
     *
     * @param project
     * @param classFileCollector
     * @param sourceSetContainer
     * @param skippyPluginExtension
     */
    public SkippifiedTestCollector(Project project, ClassFileCollector classFileCollector, SourceSetContainer sourceSetContainer, SkippyPluginExtension skippyPluginExtension) {
        this.project = project;
        this.classFileCollector = classFileCollector;
        this.sourceSetContainer = sourceSetContainer;
        this.skippyPluginExtension = skippyPluginExtension;
    }

    /**
     * Collects all {@link SkippifiedTest}s in the project.
     *
     * @return all {@link SkippifiedTest}s in the project
     */
    public List<SkippifiedTest> collect() {
        var result = new LinkedList<SkippifiedTest>();
        for (var sourceSetWithTestTask : skippyPluginExtension.getSourceSetsWithTestTasks()) {
            result.addAll(collect(sourceSetWithTestTask));
        }
        return result;
    }

    /**
     * Collects all {@link SkippifiedTest}s in the SourceSet identified by {@param sourceSetWithTestTask}.
     *
     * @return all {@link SkippifiedTest}s in the SourceSet identified by {@param sourceSetWithTestTask}
     */
    private List<SkippifiedTest> collect(SourceSetWithTestTask sourceSetWithTestTask) {
            var sourceSet = sourceSetContainer.getByName(sourceSetWithTestTask.getSourceSetName());
            var classFiles = classFileCollector.collect(sourceSet);
            return classFiles.stream()
                    .filter(classFile -> SkippyJUnit5Detector.usesSkippyJunit5Extension(classFile.getAbsolutePath()))
                    .map(classFile -> new SkippifiedTest(classFile, sourceSetWithTestTask.getTestTask()))
                    .sorted(comparing(skippifiedTest -> skippifiedTest.getFullyQualifiedClassName()))
                    .toList();
    }

}