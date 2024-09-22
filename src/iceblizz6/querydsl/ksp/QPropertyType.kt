package iceblizz6.querydsl.ksp

import com.querydsl.core.types.dsl.EnumPath
import com.querydsl.core.types.dsl.ListPath
import com.querydsl.core.types.dsl.MapPath
import com.querydsl.core.types.dsl.SetPath
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName

sealed interface QPropertyType {
    val pathTypeName: TypeName
    val pathClassName: ClassName
    val originalClassName: TypeName

    class ListCollection(
        val innerType: QPropertyType
    ) : QPropertyType {
        override val originalClassName: TypeName
            get() = List::class.asTypeName().parameterizedBy(innerType.originalClassName)

        override val pathClassName: ClassName
            get() = ListPath::class.asClassName()

        override val pathTypeName: TypeName
            get() = ListPath::class.asClassName().parameterizedBy(innerType.originalClassName, innerType.pathTypeName)
    }

    class SetCollection(
        val innerType: QPropertyType
    ) : QPropertyType {
        override val originalClassName: TypeName
            get() = Set::class.asTypeName().parameterizedBy(innerType.originalClassName)

        override val pathClassName: ClassName
            get() = SetPath::class.asClassName()

        override val pathTypeName: TypeName
            get() = SetPath::class.asClassName().parameterizedBy(innerType.originalClassName, innerType.pathTypeName)
    }

    class MapCollection(
        val keyType: QPropertyType,
        val valueType: QPropertyType
    ) : QPropertyType {
        override val originalClassName: TypeName
            get() = Map::class.asTypeName().parameterizedBy(keyType.originalClassName, valueType.originalClassName)

        override val pathClassName: ClassName
            get() = MapPath::class.asClassName()

        override val pathTypeName: TypeName
            get() = MapPath::class.asTypeName().parameterizedBy(keyType.originalClassName, valueType.originalClassName, valueType.pathTypeName)
    }

    class Simple(
        val type: SimpleType
    ) : QPropertyType {
        override val originalClassName: TypeName
            get() = type.className

        override val pathClassName: ClassName
            get() = type.pathClassName

        override val pathTypeName: TypeName
            get() = type.pathTypeName
    }

    class EnumReference(
        val enumClassName: ClassName
    ) : QPropertyType {
        override val originalClassName: TypeName
            get() = enumClassName

        override val pathClassName: ClassName
            get() = EnumPath::class.asClassName()

        override val pathTypeName: TypeName
            get() = EnumPath::class.asTypeName().parameterizedBy(enumClassName)
    }

    class ObjectReference(
        val target: QueryModel
    ) : QPropertyType {
        override val originalClassName: TypeName
            get() = target.originalClassName

        override val pathClassName: ClassName
            get() = target.className

        override val pathTypeName: TypeName
            get() = target.className
    }
}
