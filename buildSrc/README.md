# skippy-gradle-publish-plugin

Plugin that applies the necessary configuration to publish to SonaType OSSRH (OSS Repository Hosting).

Usage:
```
plugins {
    id 'io.skippy.ossrh-publish'
}
```

The title and description for the POM can be customized as follows: 
```
ossrhPublish {
    title = 'skippy-core'
    description = 'Skippy functionality that is agnostic to build tools and testing frameworks.'
}
```