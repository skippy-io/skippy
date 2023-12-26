# skippy-gradle

The Skippy plugin provides Skippy support for Gradle.

## Install

Release versions are available in Gradle's Plugin Portal:

```groovy
plugins {
    id("io.skippy") version "0.0.7"
}
```

Release versions are also available in Maven Central:
```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'io.skippy:skippy-gradle:0.0.7'
    }
}

apply plugin: io.skippy.gradle.SkippyPlugin
```

Snapshots are available in `s01.oss.sonatype.org`:

```groovy
buildscript {
    repositories {
        mavenCentral()
        maven { url = 'https://s01.oss.sonatype.org/content/repositories/snapshots/' }
    }
    dependencies {
        classpath 'io.skippy:skippy-gradle:0.0.8-SNAPSHOT'
    }
}

apply plugin: io.skippy.gradle.SkippyPlugin
```

## Tasks

The plugin adds the `skippyClean` and `skippyAnalyze` tasks to your project:
```
./gradlew tasks

...

Skippy tasks
------------
skippyClean
skippyAnalyze
```

### skippyAnalyze

`skippyAnalyze` captures 
- coverage data for each skippified test and
- a hash for each class file in the project.

The captured data is stored in the skippy directory:
```
./gradlew skippyAnalyze

> Task :skippyAnalyze
Writing skippy/classes.md5
Writing skippy/com.example.FooTest.cov
Writing skippy/com.example.BarTest.cov
...
```

The generated files are consumed by Skippy's testing libraries (e.g., [skippy-junit5](../skippy-junit5/README.md))
to determine whether a test can be skipped.

### skippyClean

`skippyClean` empties the skippy directory:

```
./gradlew skippyClean
```