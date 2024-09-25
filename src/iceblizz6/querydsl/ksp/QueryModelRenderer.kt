package iceblizz6.querydsl.ksp

import com.querydsl.core.types.Path
import com.querydsl.core.types.PathMetadata
import com.querydsl.core.types.dsl.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import iceblizz6.querydsl.ksp.Naming.toCamelCase
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.*

object QueryModelRenderer {
    fun render(model: QueryModel): TypeSpec {
        return TypeSpec.classBuilder(model.className)
            .setEntitySuperclass(model)
            .addSuperProperty(model)
            .addProperties(model)
            .constructorForPath(model)
            .constructorForMetadata(model)
            .constructorForVariable(model)
            .constructorForTypeMetadata(model)
            .addInitializerCompanionObject(model)
            .build()
    }

    private fun TypeSpec.Builder.setEntitySuperclass(model: QueryModel): TypeSpec.Builder {
        val constraint: TypeName = if (model.typeParameterCount > 0) {
            val typeParams = (0..<model.typeParameterCount).map { STAR }
            model.originalClassName.parameterizedBy(typeParams)
        } else {
            model.originalClassName
        }
        superclass(
            when (model.type) {
                QueryModel.Type.ENTITY,
                QueryModel.Type.SUPERCLASS -> EntityPathBase::class.asClassName().parameterizedBy(constraint)
                QueryModel.Type.EMBEDDABLE -> BeanPath::class.asClassName().parameterizedBy(constraint)
            }
        )
        return this
    }

    private fun TypeSpec.Builder.addSuperProperty(model: QueryModel): TypeSpec.Builder {
        model.superclass?.let { superclass ->
            val superProperty = PropertySpec
                .builder("_super", superclass.className)
                .delegate(
                    CodeBlock.builder()
                        .beginControlFlow("lazy")
                        .addStatement("${superclass.className}(this)")
                        .endControlFlow()
                        .build()
                )
                .build()
            addProperty(superProperty)
        }
        return this
    }

    private fun TypeSpec.Builder.addProperties(model: QueryModel): TypeSpec.Builder {
        model.properties
            .map { renderProperty(it) }
            .forEach { addProperty(it) }
        return this
    }

    private fun renderProperty(property: QProperty): PropertySpec {
        val name = property.name
        val type = property.type
        return when (type) {
            is QPropertyType.Simple -> renderSimpleProperty(name, type)
            is QPropertyType.EnumReference -> renderEnumReference(name, type)
            is QPropertyType.ObjectReference -> renderObjectReference(name, type)
            is QPropertyType.Unknown -> renderUnknownProperty(name, type)
            is QPropertyType.ListCollection -> {
                val inner = type.innerType
                PropertySpec
                    .builder(name, ListPath::class.asClassName().parameterizedBy(inner.originalTypeName, inner.pathTypeName))
                    .initializer("createList(\"$name\", ${inner.originalClassName}::class.java, ${inner.pathClassName}::class.java, null)")
                    .build()
            }
            is QPropertyType.MapCollection -> {
                val keyType = type.keyType
                val valueType = type.valueType
                PropertySpec
                    .builder(name, MapPath::class.asClassName().parameterizedBy(keyType.originalTypeName, valueType.originalTypeName, valueType.pathTypeName))
                    .initializer("createMap(\"$name\", ${keyType.originalClassName}::class.java, ${valueType.originalClassName}::class.java, ${valueType.pathClassName}::class.java)")
                    .build()
            }
            is QPropertyType.SetCollection -> {
                val inner = type.innerType
                PropertySpec
                    .builder(name, SetPath::class.asClassName().parameterizedBy(inner.originalTypeName, inner.pathTypeName))
                    .initializer("createSet(\"$name\", ${inner.originalClassName}::class.java, ${inner.pathClassName}::class.java, null)")
                    .build()
            }
        }
    }

    private fun renderSimpleProperty(name: String, property: QPropertyType.Simple): PropertySpec {
        return when (property.type) {
            SimpleType.ANY -> {
                PropertySpec
                    .builder(name, SimplePath::class.asClassName().parameterizedBy(Any::class.asClassName()))
                    .initializer("createSimple(\"$name\", Any::class.java)")
                    .build()
            }
            SimpleType.STRING -> {
                PropertySpec
                    .builder(name, StringPath::class.asClassName())
                    .initializer("createString(\"$name\")")
                    .build()
            }
            SimpleType.CHAR -> {
                PropertySpec
                    .builder(name, ComparablePath::class.asClassName().parameterizedBy(Char::class.asClassName()))
                    .initializer("createComparable(\"$name\", Char::class.java)")
                    .build()
            }
            SimpleType.BYTE -> {
                PropertySpec
                    .builder(name, NumberPath::class.asClassName().parameterizedBy(Byte::class.asClassName()))
                    .initializer("createNumber(\"$name\", Byte::class.java)")
                    .build()
            }
            SimpleType.INT -> {
                PropertySpec
                    .builder(name, NumberPath::class.asClassName().parameterizedBy(Int::class.asClassName()))
                    .initializer("createNumber(\"$name\", Int::class.java)")
                    .build()
            }
            SimpleType.LONG -> {
                PropertySpec
                    .builder(name, NumberPath::class.asClassName().parameterizedBy(Long::class.asClassName()))
                    .initializer("createNumber(\"$name\", Long::class.java)")
                    .build()
            }
            SimpleType.UUID -> {
                PropertySpec
                    .builder(name, ComparablePath::class.asClassName().parameterizedBy(UUID::class.asClassName()))
                    .initializer("createComparable(\"$name\", UUID::class.java)")
                    .build()
            }
            SimpleType.SHORT -> {
                PropertySpec
                    .builder(name, NumberPath::class.asClassName().parameterizedBy(Short::class.asClassName()))
                    .initializer("createNumber(\"$name\", Short::class.java)")
                    .build()
            }
            SimpleType.BIG_INTEGER -> {
                PropertySpec
                    .builder(name, NumberPath::class.asClassName().parameterizedBy(BigInteger::class.asClassName()))
                    .initializer("createNumber(\"$name\", BigInteger::class.java)")
                    .build()
            }
            SimpleType.FLOAT -> {
                PropertySpec
                    .builder(name, NumberPath::class.asClassName().parameterizedBy(Float::class.asClassName()))
                    .initializer("createNumber(\"$name\", Float::class.java)")
                    .build()
            }
            SimpleType.DOUBLE -> {
                PropertySpec
                    .builder(name, NumberPath::class.asClassName().parameterizedBy(Double::class.asClassName()))
                    .initializer("createNumber(\"$name\", Double::class.java)")
                    .build()
            }
            SimpleType.BIG_DECIMAL -> {
                PropertySpec
                    .builder(name, NumberPath::class.asClassName().parameterizedBy(BigDecimal::class.asClassName()))
                    .initializer("createNumber(\"$name\", BigDecimal::class.java)")
                    .build()
            }
            SimpleType.BOOLEAN -> {
                PropertySpec
                    .builder(name, BooleanPath::class.asClassName())
                    .initializer("createBoolean(\"$name\")")
                    .build()
            }
            SimpleType.LOCAL_DATE -> {
                PropertySpec
                    .builder(name, DatePath::class.asClassName().parameterizedBy(LocalDate::class.asClassName()))
                    .initializer("createDate(\"$name\", LocalDate::class.java)")
                    .build()
            }
            SimpleType.ZONED_DATE_TIME -> {
                PropertySpec
                    .builder(name, DateTimePath::class.asClassName().parameterizedBy(ZonedDateTime::class.asClassName()))
                    .initializer("createDateTime(\"$name\", ZonedDateTime::class.java)")
                    .build()
            }
            SimpleType.LOCAL_DATE_TIME -> {
                PropertySpec
                    .builder(name, DateTimePath::class.asClassName().parameterizedBy(LocalDateTime::class.asClassName()))
                    .initializer("createDateTime(\"$name\", LocalDateTime::class.java)")
                    .build()
            }
            SimpleType.LOCAL_TIME -> {
                PropertySpec
                    .builder(name, TimePath::class.asClassName().parameterizedBy(LocalTime::class.asClassName()))
                    .initializer("createTime(\"$name\", LocalTime::class.java)")
                    .build()
            }
            SimpleType.LOCALE -> {
                PropertySpec
                    .builder(name, SimplePath::class.asClassName().parameterizedBy(Locale::class.asClassName()))
                    .initializer("createSimple(\"$name\", Locale::class.java)")
                    .build()
            }
        }
    }

    private fun renderUnknownProperty(name: String, type: QPropertyType.Unknown) : PropertySpec {
        return PropertySpec
            .builder(name, SimplePath::class.asClassName().parameterizedBy(type.originalTypeName))
            .initializer("createSimple(\"$name\", ${type.originalClassName}::class.java)")
            .build()
    }

    private fun renderEnumReference(name: String, type: QPropertyType.EnumReference): PropertySpec {
        return PropertySpec
            .builder(name, EnumPath::class.asClassName().parameterizedBy(type.enumClassName))
            .initializer("createEnum(\"${name}\", ${type.enumClassName}::class.java)")
            .build()
    }

    private fun renderObjectReference(name: String, type: QPropertyType.ObjectReference): PropertySpec {
        return PropertySpec
            .builder(name, type.target.className)
            .delegate(
                CodeBlock.builder()
                    .beginControlFlow("lazy")
                    .addStatement("${type.target.className}(forProperty(\"${name}\"))")
                    .endControlFlow()
                    .build()
            )
            .build()
    }

    private fun TypeSpec.Builder.constructorForPath(model: QueryModel): TypeSpec.Builder {
        if (model.typeParameterCount > 0) {
            val typeParams = (0..<model.typeParameterCount).map { STAR }
            val source = model.originalClassName.parameterizedBy(typeParams)
            val spec = FunSpec.constructorBuilder()
                .addParameter("path", Path::class.asClassName().parameterizedBy(WildcardTypeName.producerOf(source)))
                .callSuperConstructor("path.type, path.metadata")
                .build()
            addFunction(spec)
        } else {
            val source = model.originalClassName
            val spec = FunSpec.constructorBuilder()
                .addParameter("path", Path::class.asClassName().parameterizedBy(WildcardTypeName.producerOf(source)))
                .callSuperConstructor("path.type, path.metadata")
                .build()
            addFunction(spec)
        }
        return this
    }

    private fun TypeSpec.Builder.constructorForMetadata(model: QueryModel): TypeSpec.Builder {
        val source = model.originalClassName
        val spec = FunSpec.constructorBuilder()
            .addParameter("metadata", PathMetadata::class)
            .callSuperConstructor("$source::class.java, metadata")
            .build()
        addFunction(spec)
        return this
    }

    private fun TypeSpec.Builder.constructorForVariable(model: QueryModel): TypeSpec.Builder {
        val spec = FunSpec.constructorBuilder()
            .addParameter("variable", String::class)
            .callSuperConstructor(
                "${model.originalClassName}::class.java",
                "${com.querydsl.core.types.PathMetadataFactory::class.qualifiedName!!}.forVariable(variable)"
            )
            .build()
        addFunction(spec)
        return this
    }

    private fun TypeSpec.Builder.constructorForTypeMetadata(model: QueryModel): TypeSpec.Builder {
        if (model.typeParameterCount > 0) {
            val typeParams = (0..<model.typeParameterCount).map { STAR }
            val source = model.originalClassName.parameterizedBy(typeParams)
            val spec = FunSpec.constructorBuilder()
                .addParameter("type", Class::class.asClassName().parameterizedBy(WildcardTypeName.producerOf(source)))
                .addParameter("metadata", PathMetadata::class)
                .callSuperConstructor("type, metadata")
                .build()
            addFunction(spec)
        } else {
            val source = model.originalClassName
            val spec = FunSpec.constructorBuilder()
                .addParameter("type", Class::class.asClassName().parameterizedBy(WildcardTypeName.producerOf(source)))
                .addParameter("metadata", PathMetadata::class)
                .callSuperConstructor("type, metadata")
                .build()
            addFunction(spec)
        }
        return this
    }

    private fun TypeSpec.Builder.addInitializerCompanionObject(model: QueryModel): TypeSpec.Builder {
        val source = model.originalClassName
        val qSource = model.className
        val name = source.simpleName.toCamelCase()
        val companionObject = TypeSpec.companionObjectBuilder()
            .addProperty(
                PropertySpec.builder(name, qSource)
                    .initializer("$qSource(\"${name}\")")
                    .addAnnotation(JvmField::class)
                    .build()
            )
            .build()
        addType(companionObject)
        return this
    }
}
