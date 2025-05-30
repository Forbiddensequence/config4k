
# Guide to migrate from 0.x.x version to 1.x.x

Library since 1.x.x uses [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) to config parsing, so library api was changed. 
## Common config changes
First of all, all extracted config should be marked with @Serializable annotation, and all nested objects should be marked same. 

For example
```kotlin
data class TestConf(
    val foo: String
)
```

should become

```kotlin
@Serializable
data class TestConf(
    val foo: String
)
```
## Built-in contextual class
It is not possible to mark all classes with that annotation, and for that case if you got java class, that was previously parseble with that library (`java.util.UUID`, `java.net.URL`, `java.time.Duration`), you should use @Contextual. 

Library supports these contextual types by default

- `java.io.File`
- `java.nio.file.Path`
- `java.time.Duration`
- `java.time.Period`
- `java.time.temporal.TemporalAmount`
- `java.util.UUID`
- `java.net.URL`
- `java.net.URI`
- `kotlin.text.Regex`
- `java.util.regex.Pattern`

For migration, you should replace that 
```kotlin
data class TestConf(
    val uuid: UUID
)
```

should become

```kotlin
@Serializable
data class TestConf(
    @Contextual
    val uuid: UUID
)
```

## Functions to config extraction
Functions to config extraction are also changed, by now you shouldn't use extension function from `io.github.config4k.Extension.kt` file. 
Instead of it you should use [Config4k](src/main/kotlin/io/github/config4k/Config4k.kt) object to serialize/deserialize your config.
For example 

```kotlin
config.extract<TestJavaBean>()
```

should become

```kotlin
Config4k.decodeFromConfig<TestJavaBean>(config)
```
And parsing from code objects to config should become
```kotlin
Conf("foo = bar".toConfig()).toConfig("person")
```

should become

```kotlin 
Config4k.encodeToConfig(Conf("foo = bar".toConfig()))
```

## Config by path extraction and property delegation
Library has lost ability to work with property delegation extraction and config by path extraction, so there are not equivalents of such construction

```kotlin 
val config =
    ConfigFactory.parseString(
        """
        |stringValue = hello
        |
                """.trimMargin(),
    )

val stringValue: String by config
```
And 

```kotlin 
val config = ConfigFactory.parseString("""value = true""")
config.extract<Boolean>("value") 
```

## Map as list support

Functionality that supports extraction of maps in list with key value properties is still supported, so that config is still parseble
```hocon 
map = [
  {key = 5, value = "foo"},
  {key = 0, value = "bar"}
]
```
You should use [MapAsList](src/main/kotlin/io/github/config4k/TypeAliases.kt:8) type for it
