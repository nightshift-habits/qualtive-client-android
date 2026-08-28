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

First of all, make sure you have created a question on [qualtive.io](https://qualtive.io). Each feedback entry is posted to an enquiry (ID or slug) within your container.

Optionally pass a workspace slug. When omitted, the user API uses the container's default workspace.

```kotlin
val qualtive = Qualtive(context, containerId = "my-company", workspaceId = "my-department")
```

### Fetching an enquiry

```kotlin
val qualtive = Qualtive(context, containerId = "my-company")
val enquiry = qualtive.fetchEnquiry("my-question")
```

### Using custom UI

To post a feedback entry:

```kotlin
val qualtive = Qualtive(context, containerId = "my-company")

qualtive.post(
    enquiryId = "my-question",
    content = listOf(
        Entry.Content.Score(value = 75),
        Entry.Content.Text(value = "Hello world!"),
    ),
)
```

If users can log in on your app, include a user describing them:

```kotlin
qualtive.post(
    enquiryId = "my-question",
    content = listOf(
        Entry.Content.Score(value = 75),
        Entry.Content.Text(value = "Hello world!"),
    ),
    user = User(
        id = "user-123",
        name = "Steve",
        email = "steve@gmail.com",
    ),
)
```

You can include custom attributes that will be shown on [qualtive.io](https://qualtive.io):

```kotlin
qualtive.post(
    enquiryId = "my-question",
    content = listOf(
        Entry.Content.Score(value = 75),
    ),
    customAttributes = mapOf(
        "age" to 22,
    ),
)
```

Privacy options apply to that post only:

```kotlin
qualtive.post(
    enquiryId = "my-question",
    content = listOf(Entry.Content.Text(value = "Hello")),
    options = PostOptions(
        metadataCollection = MetadataCollection.NonPersonal,
        userTrackingConsent = UserTrackingConsent.Granted,
    ),
)
```

Attachments (for example from the photo picker) upload first, then reference the returned id:

```kotlin
val image = qualtive.uploadAttachment(uri)
qualtive.post(
    enquiryId = "my-question",
    content = listOf(
        Entry.Content.Attachments(attachments = listOf(image)),
    ),
)
```
