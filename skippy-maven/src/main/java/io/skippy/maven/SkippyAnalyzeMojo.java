package io.skippy.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * WIP
 */
@Mojo(name = "analyze", defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES)
public class SkippyAnalyzeMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Override
    public void execute() {
        project.getProperties().setProperty("skippyEmitCovFiles", "true");
        getLog().warn(project.getName());
    }

}
