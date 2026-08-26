# Qualtive Client Library for Android

## Installation

Add the library as a Gradle dependency:

```
implementation("io.qualtive:client:0.1.0-SNAPSHOT")
```

Snapshots are published to the Maven Central snapshot repository:

```
repositories {
    mavenCentral()
    maven("https://central.sonatype.com/repository/maven-snapshots/")
}
```

Minimum Android version: API 24 (Android 7).

This library is Kotlin-only. Java is not a supported public API.

## Usage

```kotlin
val qualtive = Qualtive(context, containerId = "my-company")
val enquiry = qualtive.fetchEnquiry("nps")
```
