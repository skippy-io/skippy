# skippy-junit5

Skippy support for JUnit 4.

## Step 1: Add The skippy-junit5 Dependency

Releases are available in Maven Central:
```groovy
repositories {
    mavenCentral()
}

dependencies {
    testImplementation 'io.skippy:skippy-junit4:0.0.7'
}
```

Snapshots are available in `s01.oss.sonatype.org`:
```groovy
repositories {
    maven { url = 'https://s01.oss.sonatype.org/content/repositories/snapshots/' }
}

dependencies {
    testImplementation 'io.skippy:skippy-junit4:0.0.8-SNAPSHOT'
}
```

## Step 2: Skippify Your Tests

WIP