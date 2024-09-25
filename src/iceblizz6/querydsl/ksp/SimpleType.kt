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
    val detectedClassNames: List<ClassName>,
    val className: ClassName,
    val pathClassName: ClassName,
    val pathTypeName: TypeName
) {
    ANY(
        listOf(Any::class.asClassName()),
        Any::class.asClassName(),
        SimplePath::class.asClassName(),
        SimplePath::class.asClassName().parameterizedBy(Any::class.asClassName())
    ),
    CHAR(
        listOf(Char::class.asClassName()),
        Char::class.asClassName(),
        ComparablePath::class.asClassName(),
        ComparablePath::class.asClassName().parameterizedBy(Char::class.asClassName())
    ),
    STRING(
        listOf(String::class.asClassName()),
        String::class.asClassName(),
        StringPath::class.asClassName(),
        StringPath::class.asTypeName()
    ),
    UUID(
        listOf(java.util.UUID::class.asClassName()),
        java.util.UUID::class.asClassName(),
        ComparablePath::class.asClassName(),
        ComparablePath::class.asClassName().parameterizedBy(java.util.UUID::class.asClassName())
    ),
    BYTE(
        listOf(Byte::class.asClassName(), UByte::class.asClassName()),
        Byte::class.asClassName(),
        NumberPath::class.asClassName(),
        NumberPath::class.parameterizedBy(Byte::class)
    ),
    SHORT(
        listOf(Short::class.asClassName(), UShort::class.asClassName()),
        Short::class.asClassName(),
        NumberPath::class.asClassName(),
        NumberPath::class.parameterizedBy(Short::class)
    ),
    INT(
        listOf(Int::class.asClassName(), UInt::class.asClassName()),
        Int::class.asClassName(),
        NumberPath::class.asClassName(),
        NumberPath::class.parameterizedBy(Int::class)
    ),
    BIG_INTEGER(
        listOf(BigInteger::class.asClassName()),
        BigInteger::class.asClassName(),
        NumberPath::class.asClassName(),
        NumberPath::class.parameterizedBy(BigInteger::class)
    ),
    LONG(
        listOf(Long::class.asClassName(), ULong::class.asClassName()),
        Long::class.asClassName(),
        NumberPath::class.asClassName(),
        NumberPath::class.parameterizedBy(Long::class)
    ),
    FLOAT(
        listOf(Float::class.asClassName(), Float::class.asClassName()),
        Float::class.asClassName(),
        NumberPath::class.asClassName(),
        NumberPath::class.parameterizedBy(Float::class)
    ),
    DOUBLE(
        listOf(Double::class.asClassName()),
        Double::class.asClassName(),
        NumberPath::class.asClassName(),
        NumberPath::class.parameterizedBy(Double::class)
    ),
    BIG_DECIMAL(
        listOf(BigDecimal::class.asClassName()),
        BigDecimal::class.asClassName(),
        NumberPath::class.asClassName(),
        NumberPath::class.parameterizedBy(BigDecimal::class)
    ),
    BOOLEAN(
        listOf(Boolean::class.asClassName()),
        Boolean::class.asClassName(),
        BooleanPath::class.asClassName(),
        BooleanPath::class.asTypeName()
    ),
    LOCAL_DATE(
        listOf(LocalDate::class.asClassName()),
        LocalDate::class.asClassName(),
        DatePath::class.asClassName(),
        DatePath::class.parameterizedBy(LocalDate::class)
    ),
    ZONED_DATE_TIME(
        listOf(ZonedDateTime::class.asClassName()),
        ZonedDateTime::class.asClassName(),
        DateTimePath::class.asClassName(),
        DateTimePath::class.parameterizedBy(ZonedDateTime::class)
    ),
    LOCAL_DATE_TIME(
        listOf(LocalDateTime::class.asClassName()),
        LocalDateTime::class.asClassName(),
        DateTimePath::class.asClassName(),
        DateTimePath::class.parameterizedBy(LocalDateTime::class)
    ),
    LOCAL_TIME(
        listOf(LocalTime::class.asClassName()),
        LocalTime::class.asClassName(),
        TimePath::class.asClassName(),
        TimePath::class.parameterizedBy(LocalTime::class)
    ),
    LOCALE(
        listOf(Locale::class.asClassName()),
        Locale::class.asClassName(),
        SimplePath::class.asClassName(),
        SimplePath::class.parameterizedBy(Locale::class)
    ),
}
