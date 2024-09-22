package iceblizz6.querydsl.ksp

object Naming {
    fun String.toPascalCase(): String {
        return this.first().uppercase() + this.substring(1)
    }

    fun String.toCamelCase(): String {
        return this.first().lowercase() + this.substring(1)
    }
}