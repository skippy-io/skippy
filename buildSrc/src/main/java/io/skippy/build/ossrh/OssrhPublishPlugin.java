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

package io.skippy.build.ossrh;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.plugins.signing.SigningExtension;
import org.gradle.plugins.signing.SigningPlugin;

/**
 * Internal plugin that applies the necessary configuration to publish to SonaType OSSRH (OSS Repository Hosting).
 *
 * @author Florian McKee
 */
public class OssrhPublishPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        var extension = project.getExtensions().create("ossrhPublish", OssrhPublishPluginExtension.class);
        project.afterEvaluate(a -> applyInternal(project, extension));
    }


    private void applyInternal(Project project, OssrhPublishPluginExtension extension) {

        project.getPlugins().apply(MavenPublishPlugin.class);
        project.getPlugins().apply(SigningPlugin.class);

        project.getTasks().register("javadocJar", Jar.class, task -> {
            task.getArchiveClassifier().set("javadoc");
            task.from(project.getTasks().getByName("javadoc"));
        });

        project.getTasks().register("sourcesJar", Jar.class, task -> {
            task.getArchiveClassifier().set("sources");
            var sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
            task.from(sourceSets.getByName("main").getAllSource());
        });

        project.getArtifacts().add("archives", project.getTasks().getByName("javadocJar"));
        project.getArtifacts().add("archives", project.getTasks().getByName("sourcesJar"));

        project.getExtensions().configure(PublishingExtension.class, publishing -> {

            publishing.getRepositories().maven(repo -> {
                    repo.setName("OSSRH");
                    if (project.getVersion().toString().endsWith("SNAPSHOT")) {
                        repo.setUrl("https://s01.oss.sonatype.org/content/repositories/snapshots/");
                    } else {
                        repo.setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/");
                    }
                    repo.credentials(credentials -> {
                        credentials.setUsername(System.getenv("OSSRH_USERNAME"));
                        credentials.setPassword(System.getenv("OSSRH_PASSWORD"));
                    });
            });

            publishing.getPublications().create("maven", MavenPublication.class, publication -> {
                publication.from(project.getComponents().getByName("java"));
                publication.artifact(project.getTasks().getByName("sourcesJar"));
                publication.artifact(project.getTasks().getByName("javadocJar"));

                publication.pom(pom -> {
                    pom.getName().set(extension.getTitle());
                    pom.getDescription().set(extension.getDescription());
                    pom.getUrl().set("https://github.com/skippy-io/skippy");

                    pom.licenses(licenses -> {
                        licenses.license(license -> {
                            license.getName().set("The Apache License, Version 2.0");
                            license.getUrl().set("http://www.apache.org/licenses/LICENSE-2.0.txt");
                        });
                    });

                    pom.developers(developers -> {
                        developers.developer(developer -> {
                            developer.getId().set("fmck3516");
                            developer.getName().set("Florian McKee");
                            developer.getEmail().set("contact@skippy.io");
                        });
                    });


                    pom.issueManagement(issues -> {
                        issues.getSystem().set("GitHub");
                        issues.getUrl().set("https://github.com/skippy-io/skippy/issues");
                    });

                    pom.scm(scm -> {
                        scm.getConnection().set("scm:git:git://github.com/skippy-io/skippy.git");
                        scm.getDeveloperConnection().set("scm:git:ssh://git@github.com/skippy-io/skippy.git");
                        scm.getUrl().set("https://github.com/skippy-io/skippy");
                    });
                });

                var signingKey = System.getenv("SKIPPY_PRIVATE_KEY");
                var signingKeySecret = System.getenv("SKIPPY_PRIVATE_KEY_SECRET");

                if (signingKey != null && signingKeySecret != null) {
                    project.getExtensions().configure(SigningExtension.class, signing -> {
                        signing.useInMemoryPgpKeys(signingKey, signingKeySecret);
                        signing.sign(publication);
                    });
                } else {
                    project.getLogger().debug("Environment variables SKIPPY_PRIVATE_KEY / SKIPPY_PRIVATE_KEY_SECRET not set: Skipping signing of artifacts.");
                }
            });
        });

        project.getTasks().register("publishSnapshot", task -> {
            task.dependsOn("publish");
        });

        project.getGradle().getTaskGraph().whenReady(graph -> {
            project.getTasks().getByName("publishMavenPublicationToOSSRHRepository").onlyIf(task -> {

                /*
                 * Ensures the following:
                 * - The :publish task that is invoked when a new release is created in GitHub only publishes release versions.
                 * - The :publishSnapshot task that is invoked when a change is merged to main only publishes snapshot versions.
                 *
                 * Reasoning:
                 * - We don't want a merge to main with a release version to trigger a publication to Maven Central. This should
                 *   only happen when a release is created in GitHub.
                 * - We don't want folks to create releases in GitHub to publish snapshot versions. This can be done by simply
                 *   merging a change with a snapshot version to main.
                 */

                var version = project.getVersion().toString();

                var snapshotDeployment = version.endsWith("SNAPSHOT") && graph.hasTask(":%s:publishSnapshot".formatted(project.getName()));
                var releaseDeployment = ! version.endsWith("SNAPSHOT") && ! graph.hasTask(":%s:publishSnapshot".formatted(project.getName()));

                return snapshotDeployment || releaseDeployment;
            });
        });

    }
}
