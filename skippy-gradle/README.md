# skippy-gradle-plugin

The Skippy plugin provides Skippy support in Gradle.

## Apply the Skippy Plugin

First, you have to apply the Skippy plugin:
```groovy
buildscript {
    repositories {
        
        // for releases
        mavenCentral()
        
        // for snapshots
        maven { url = 'https://s01.oss.sonatype.org/content/repositories/snapshots/' }
    }
    dependencies {
        classpath 'io.skippy:skippy-gradle:0.0.7-SNAPSHOT'
    }
}

apply plugin: io.skippy.gradle.SkippyPlugin
```
Note: The Skippy plugin automatically applies the `Java` plugin.

## Tasks

The plugin adds a couple of new tasks to your project:
```console
./gradlew tasks
```
You will see the following output:
```console
Skippy tasks
------------
skippyClean
skippyAnalyze
```

### skippyAnalyze Task

`skippyAnalyze` captures 
- execution data for each test that uses Skippy and 
- a hash for each class file in the project.

Example:
```console
./gradlew skippyAnalyze
```

You should see something like this:

```console
./gradlew clean skippyAnalyze                                  

> Task skippyCoverage_com.example.Class0Test
Skippy: Capturing coverage data for com.example.Class0Test in skippy/com.example.Class0Test.csv

> Task skippyCoverage_com.example.Class1Test
Skippy: Capturing coverage data for com.example.Class1Test in skippy/com.example.Class1Test.csv

> Task skippyCoverage_com.example.Class2Test
Skippy: Capturing coverage data for com.example.Class2Test in skippy/com.example.Class2Test.csv

> Task skippyAnalyze
Skippy: Capturing a snapshot of all source files in skippy/sourceSnapshot.md5
```

The generated data is consumed by Skippy's testing libraries (e.g., [skippy-junit5](../skippy-junit5/README.md)).

### skippyClean Task

`skippyClean` removes previously captured execution data and the file that contains a hash for every source file:

```console
./gradlew skippyClean
```

## Customization

By default, `skippyAnalyze` will 
- look for skippified tests in the `test` SourceSet and
- execute skippifed tests using the `test` task.

Builds can customize this behavior using the `skippy` DSL block: 

```groovy
skippy {
    sourceSet {
        name = 'test'
        testTask = 'test'
    }
    sourceSet {
        name = 'intTest'
        testTask = 'integrationTest'
    }
}
```

The above example reads as follows:
- Skippy will look for skippified tests in the SourceSets `test` and `intTest`
- Skippified tests in the `test` SourceSet will be executed using the `test` task
- Skippified tests in the `intTest` SourceSet will be executed using the `integrationTest` task
