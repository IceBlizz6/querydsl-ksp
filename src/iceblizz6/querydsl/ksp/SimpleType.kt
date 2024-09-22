package iceblizz6.querydsl.ksp

import com.querydsl.core.types.dsl.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.*

enum class SimpleType(
    val className: ClassName,
    val pathClassName: ClassName,
    val pathTypeName: TypeName
) {
    CHAR(
        Char::class.asClassName(),
        ComparablePath::class.asClassName(),
        ComparablePath::class.asClassName().parameterizedBy(Char::class.asClassName())
    ),
    STRING(
        String::class.asClassName(),
        StringPath::class.asClassName(),
        StringPath::class.asTypeName()
    ),
    UUID(
        java.util.UUID::class.asClassName(),
        ComparablePath::class.asClassName(),
        ComparablePath::class.asClassName().parameterizedBy(java.util.UUID::class.asClassName())
    ),
    SHORT(
        Short::class.asClassName(),
        NumberPath::class.asClassName(),
        NumberPath::class.parameterizedBy(Short::class)
    ),
    INT(
        Int::class.asClassName(),
        NumberPath::class.asClassName(),
        NumberPath::class.parameterizedBy(Int::class)
    ),
    LONG(
        Long::class.asClassName(),
        NumberPath::class.asClassName(),
        NumberPath::class.parameterizedBy(Long::class)
    ),
    BIG_INTEGER(
        BigInteger::class.asClassName(),
        NumberPath::class.asClassName(),
        NumberPath::class.parameterizedBy(BigInteger::class)
    ),
    FLOAT(
        Float::class.asClassName(),
        NumberPath::class.asClassName(),
        NumberPath::class.parameterizedBy(Float::class)
    ),
    DOUBLE(
        Double::class.asClassName(),
        NumberPath::class.asClassName(),
        NumberPath::class.parameterizedBy(Double::class)
    ),
    BIG_DECIMAL(
        BigDecimal::class.asClassName(),
        NumberPath::class.asClassName(),
        NumberPath::class.parameterizedBy(BigDecimal::class)
    ),
    BOOLEAN(
        Boolean::class.asClassName(),
        BooleanPath::class.asClassName(),
        BooleanPath::class.asTypeName()
    ),
    LOCAL_DATE(
        LocalDate::class.asClassName(),
        DatePath::class.asClassName(),
        DatePath::class.parameterizedBy(LocalDate::class)
    ),
    ZONED_DATE_TIME(
        ZonedDateTime::class.asClassName(),
        DateTimePath::class.asClassName(),
        DateTimePath::class.parameterizedBy(ZonedDateTime::class)
    ),
    LOCAL_DATE_TIME(
        LocalDateTime::class.asClassName(),
        DateTimePath::class.asClassName(),
        DateTimePath::class.parameterizedBy(LocalDateTime::class)
    ),
    LOCAL_TIME(
        LocalTime::class.asClassName(),
        TimePath::class.asClassName(),
        TimePath::class.parameterizedBy(LocalTime::class)
    ),
    LOCALE(
        Locale::class.asClassName(),
        SimplePath::class.asClassName(),
        SimplePath::class.parameterizedBy(Locale::class)
    ),
}
