# Karna

Karna is a JSON parser for Kotlin. It is a modern fork of [Klaxon](https://github.com/cbeust/klaxon).

## Features

Karna provides a JSON streaming API to process large files.  
There are two implementations of the Streaming API:
- `JsonReader` for callback based processing
- `JsonCoroutineReader` for coroutine based processing

A sample usage is:

```kotlin
val objectString = """{
     "name" : "Joe",
     "age" : 23,
     "flag" : true,
     "array" : [1, 3],
     "obj1" : { "a" : 1, "b" : 2 }
}"""

JsonCoroutineReader(StringReader(objectString)).use { reader ->
    reader.beginObject() {
        var name: String? = null
        var age: Int? = null
        var flag: Boolean? = null
        var array: List<Any> = arrayListOf<Any>()
        var obj1: JsonObject? = null
        while (reader.hasNext()) {
            val readName = reader.nextName()
            when (readName) {
                "name" -> name = reader.nextString()
                "age" -> age = reader.nextInt()
                "flag" -> flag = reader.nextBoolean()
                "array" -> array = reader.nextArray()
                "obj1" -> obj1 = reader.nextObject()
                else -> Assert.fail("Unexpected name: $readName")
            }
        }
    }
}
```

All other functionality from [Klaxon](https://github.com/cbeust/klaxon) is also available.

## Install

Maven dependency

```xml
<dependency>
    <groupId>io.github.rrevo</groupId>
    <artifactId>karna</artifactId>
    <version>0.5.0</version>
</dependency>
```


## Build commands

### Local build

```bash
mvn compile test
```

### Deploy

NOTE: Terminal will prompt for gpg passphrase.

```bash
mvn clean package dokka:javadocJar gpg:sign deploy
```

### Manual Release steps

- Local git
  - Update version in `pom.xml` to remove `-SNAPSHOT`.
  - Update this file with the new version in the install section.
  - Commit.
- Deploy to Maven Central
- Create a new tag in git. Push the tag.
- Update version in `pom.xml` to next `-SNAPSHOT`. Local commit.
- Push to remote main.
- Create a new release in GitHub
- Celebrate 🎉
