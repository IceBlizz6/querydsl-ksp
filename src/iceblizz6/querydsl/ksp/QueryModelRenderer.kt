package iceblizz6.querydsl.ksp

import com.querydsl.core.types.Path
import com.querydsl.core.types.PathMetadata
import com.querydsl.core.types.dsl.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import iceblizz6.querydsl.ksp.Naming.toCamelCase

private const val INTERFACE_NAME="Interface"

object QueryModelRenderer {

    private val QueryModel.interfaceName : ClassName
        get() = ClassName(className.packageName,"${className.simpleName}$INTERFACE_NAME")

    fun renderInterface(model: QueryModel) : TypeSpec {
        return TypeSpec.interfaceBuilder(model.interfaceName).run {
            model.properties.forEach { property ->
                addProperty(renderProperty(property,true))
            }
            model.superclass?.run {
                addSuperinterface(interfaceName)
            }
            build()
        }
    }

    fun renderClass(model: QueryModel): TypeSpec {
        return TypeSpec.classBuilder(model.className)
            .setEntitySuperclass(model)
            .addSuperProperty(model)
            .primaryConstructor(model)
            .addEntitySuperInterfaces(model)
            .addProperties(model)
            .constructorForTypeMetadata(model)
            .constructorForPath(model)
            .constructorForMetadata(model)
            .constructorForVariable(model)
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
        addSuperclassConstructorParameter("type,metadata")
        return this
    }

    private fun TypeSpec.Builder.addEntitySuperInterfaces(model: QueryModel) : TypeSpec.Builder {
        addSuperinterface(model.interfaceName)
        model.superclass?.run {
            addSuperinterface(interfaceName,"_super")
        }
        return this
    }

    private fun TypeSpec.Builder.addSuperProperty(model: QueryModel): TypeSpec.Builder {
        model.superclass?.let { superclass ->
            val superProperty = PropertySpec
                .builder("_super", ClassName(superclass.className.packageName,superclass.className.simpleName))
                .initializer("_super")
                .build()
            addProperty(superProperty)
        }
        return this
    }

    private fun TypeSpec.Builder.addProperties(model: QueryModel): TypeSpec.Builder {
        model.properties
            .map(::renderProperty)
            .forEach(::addProperty)
        return this
    }

    private fun renderProperty(property: QProperty,forInterface: Boolean=false): PropertySpec {
        val name = property.name
        val type = property.type
        return when (type) {
            is QPropertyType.Simple -> property.type.type.render(name,forInterface)
            is QPropertyType.EnumReference -> renderEnumReference(name, type,forInterface)
            is QPropertyType.ObjectReference -> renderObjectReference(name, type,forInterface)
            is QPropertyType.Unknown -> renderUnknownProperty(name, type,forInterface)
            is QPropertyType.ListCollection -> {
                val inner = type.innerType
                SimpleType
                    .getPropertySpec(name, ListPath::class.asClassName().parameterizedBy(inner.originalTypeName, inner.pathTypeName),forInterface) {
                        initializer("createList(\"$name\", ${inner.originalClassName}::class.java, ${inner.pathClassName}::class.java, null)")
                    }
            }
            is QPropertyType.MapCollection -> {
                val keyType = type.keyType
                val valueType = type.valueType
                SimpleType
                    .getPropertySpec(name, MapPath::class.asClassName().parameterizedBy(keyType.originalTypeName, valueType.originalTypeName, valueType.pathTypeName),forInterface) {
                        initializer("createMap(\"$name\", ${keyType.originalClassName}::class.java, ${valueType.originalClassName}::class.java, ${valueType.pathClassName}::class.java)")
                    }
            }
            is QPropertyType.SetCollection -> {
                val inner = type.innerType
                SimpleType
                    .getPropertySpec(name, SetPath::class.asClassName().parameterizedBy(inner.originalTypeName, inner.pathTypeName),forInterface) {
                        initializer("createSet(\"$name\", ${inner.originalClassName}::class.java, ${inner.pathClassName}::class.java, null)")
                    }
            }
        }
    }

    private fun renderUnknownProperty(name: String, type: QPropertyType.Unknown,forInterface: Boolean) : PropertySpec {
        return SimpleType
            .getPropertySpec(name, SimplePath::class.asClassName().parameterizedBy(type.originalTypeName),forInterface) {
                initializer("createSimple(\"$name\", ${type.originalClassName}::class.java)")
            }
    }

    private fun renderEnumReference(name: String, type: QPropertyType.EnumReference,forInterface: Boolean): PropertySpec {
        return SimpleType
            .getPropertySpec(name, EnumPath::class.asClassName().parameterizedBy(type.enumClassName),forInterface) {
                initializer("createEnum(\"${name}\", ${type.enumClassName}::class.java)")
            }
    }

    private fun renderObjectReference(name: String, type: QPropertyType.ObjectReference,forInterface: Boolean): PropertySpec {
        return SimpleType
            .getPropertySpec(name, type.target.className,forInterface) {
                delegate(
                    CodeBlock.builder()
                        .beginControlFlow("lazy")
                        .addStatement("${type.target.className}(forProperty(\"${name}\"))")
                        .endControlFlow()
                        .build()
                )
            }
    }

    private fun TypeSpec.Builder.constructorForTypeMetadata(model: QueryModel): TypeSpec.Builder {
        if (model.superclass==null) return this
        val source = model.originalClassName.run {
            if (model.typeParameterCount > 0) parameterizedBy((0..<model.typeParameterCount).map { STAR })
            else this
        }
        val spec = FunSpec.constructorBuilder()
            .addParameter("type", Class::class.asClassName().parameterizedBy(WildcardTypeName.producerOf(source)))
            .addParameter("metadata", PathMetadata::class)
            .callThisConstructor(
                "type,metadata".run {
                    if (model.superclass==null) this
                    else plus(",${model.superclass!!.className}(type,metadata)")
                }
            )
            .build()
        addFunction(spec)
        return this
    }

    private fun TypeSpec.Builder.primaryConstructor(model: QueryModel): TypeSpec.Builder {
        val source = model.originalClassName.run {
            if (model.typeParameterCount > 0) parameterizedBy(List(model.typeParameterCount) { STAR })
            else this
        }
        val spec = FunSpec.constructorBuilder()
            .apply {if (model.superclass!=null) addModifiers(KModifier.PRIVATE) }
            .addParameter("type", Class::class.asClassName().parameterizedBy(WildcardTypeName.producerOf(source)))
            .addParameter("metadata", PathMetadata::class)
            .apply {
                model.superclass?.run {
                    addParameter(
                        ParameterSpec
                            .builder("_super", className)
                            .defaultValue("${className}(type,metadata)").build()
                    )
                }
            }
            .build()
        primaryConstructor(spec)
        return this
    }

    private fun TypeSpec.Builder.constructorForPath(model: QueryModel): TypeSpec.Builder {
        if (model.typeParameterCount > 0) {
            val typeParams = (0..<model.typeParameterCount).map { STAR }
            val source = model.originalClassName.parameterizedBy(typeParams)
            val spec = FunSpec.constructorBuilder()
                .addParameter("path", Path::class.asClassName().parameterizedBy(WildcardTypeName.producerOf(source)))
                .callThisConstructor("path.type, path.metadata")
                .build()
            addFunction(spec)
        } else {
            val source = model.originalClassName
            val spec = FunSpec.constructorBuilder()
                .addParameter("path", Path::class.asClassName().parameterizedBy(WildcardTypeName.producerOf(source)))
                .callThisConstructor("path.type, path.metadata")
                .build()
            addFunction(spec)
        }
        return this
    }

    private fun TypeSpec.Builder.constructorForMetadata(model: QueryModel): TypeSpec.Builder {
        val source = model.originalClassName
        val spec = FunSpec.constructorBuilder()
            .addParameter("metadata", PathMetadata::class)
            .callThisConstructor("$source::class.java, metadata")
            .build()
        addFunction(spec)
        return this
    }

    private fun TypeSpec.Builder.constructorForVariable(model: QueryModel): TypeSpec.Builder {
        val spec = FunSpec.constructorBuilder()
            .addParameter("variable", String::class)
            .callThisConstructor(
                "${model.originalClassName}::class.java",
                "${com.querydsl.core.types.PathMetadataFactory::class.qualifiedName!!}.forVariable(variable)"
            )
            .build()
        addFunction(spec)
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
