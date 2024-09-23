plugins {
	kotlin("jvm") version "2.0.20"
}

repositories {
	mavenCentral()
}

sourceSets.main {
	kotlin.srcDir("src")
	resources.srcDir("resources")
}

sourceSets.test {
	kotlin.srcDir("test")
}

dependencies {
	implementation("com.google.devtools.ksp:symbol-processing-api:2.0.20-1.0.25")
	implementation("com.squareup:kotlinpoet:1.18.1")
	implementation("com.squareup:kotlinpoet-ksp:1.18.1")
	implementation("jakarta.persistence:jakarta.persistence-api:3.2.0")
	implementation("com.querydsl:querydsl-core:5.1.0")
}
