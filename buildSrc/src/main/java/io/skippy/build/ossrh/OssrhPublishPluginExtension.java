package io.skippy.build.ossrh;

public class OssrhPublishPluginExtension {

    // e.g., Skippy Core
    private String title;

    // e.g., Functionality that is agnostic to build tools and testing frameworks.
    private String description;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
