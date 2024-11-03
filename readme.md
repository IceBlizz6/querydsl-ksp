# KSP for QueryDSL

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-blue.svg)](https://kotlinlang.org)
[![QueryDSL](https://img.shields.io/badge/QueryDSL-5.1.0-blue.svg)](http://www.querydsl.com)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/licenses/MIT)

A Kotlin Symbol Processing (KSP) project that enables code generation for [QueryDSL](https://github.com/querydsl/querydsl), providing a lightweight and efficient way to generate `Q` classes for QueryDSL usage in Kotlin projects.

Currently only supports jakarta annotations.

Feel free to give feedback if you want support for other annotations.

## Setup

### Gradle
Add the KSP plugin and dependency for the processor in your `build.gradle.kts`

```kotlin
plugins {
    kotlin("jvm") version "2.0.21"
    id("com.google.devtools.ksp") version "2.0.21-1.0.26"
}

repositories {
    maven { 
        url = uri("https://jitpack.io") 
    }
}

dependencies {
    implementation("com.querydsl:querydsl-core:5.1.0")
    ksp("com.github.IceBlizz6:querydsl-ksp:0.0.4")
}
```

And it may be necessary to [make your IDE aware of KSP generated code](https://kotlinlang.org/docs/ksp-quickstart.html#make-ide-aware-of-generated-code)

```kotlin
kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}
```

### Settings (all optional)

| Name           |  Type                       | Default       | Description |
|:---------------|:----------------------------|:--------------|:------|
|enable          |Boolean                      |true           |Set to false will disable processing|
|indent          |String                       |" " (4 spaces)|Indent used in generated code files|
|prefix          |String                       |"Q"            |Prefix applied to generated classes|
|suffix          |String                       |""             |Suffix applied to generated classes|
|packageSuffix   |String                       |""             |Suffix applied to package name of generated classes|
|excludedPackages|String (comma separated list)|""             |List of packages that will be skipped in processing|
|excludedClasses |String (comma separated list)|""             |List of classes that will not be processed|
|includedPackages|String (comma separated list)|""             |List of packages included, empty means it will include everything|
|includedClasses |String (comma separated list)|""             |List of classes included, empty means it will include all|

Settings must be prefixed with 'querydsl.'

Add into your `build.gradle.kts` to configure.

```kotlin
// Example
ksp {
    arg("querydsl.prefix", "QQ")
    arg("querydsl.excludedPackages", "com.example, com.sample")
}
```

## Note
* This is my first open source project so have patience and give feedback.
* Releases may be moved to maven central at a later point.
