package iceblizz6.querydsl.ksp

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ksp.toClassName

class TypeExtractor(
    private val property: KSPropertyDeclaration,
    private val models: List<QueryModel>
) {
    fun extract(type: KSType): QPropertyType {
        return simpleType(type)
            ?: collectionType(type)
            ?: referenceType(type)
            ?: throwError("Type was not recognised, This may be an entity that has not been annotated with @Entity, or maybe you are using javax instead of jakarta.")
    }

    private fun simpleType(type: KSType): QPropertyType.Simple? {
        val className = type.toClassName()
        val simpleType = SimpleType.entries.singleOrNull { it.detectedClassNames.contains(className) }
        if (simpleType == null) {
            return null
        } else {
            return QPropertyType.Simple(simpleType)
        }
    }

    private fun collectionType(type: KSType): QPropertyType? {
        return when (type.declaration.qualifiedName!!.asString()) {
            Array::class.qualifiedName!! -> throwError("Unable to process type Array, Consider using List or Set instead")
            Set::class.qualifiedName!!, "kotlin.collections.MutableSet" -> {
                val innerType = extract(type.arguments.single().type!!.resolve())
                return QPropertyType.SetCollection(innerType)
            }
            List::class.qualifiedName!!, "kotlin.collections.MutableList" -> {
                val innerType = extract(type.arguments.single().type!!.resolve())
                return QPropertyType.ListCollection(innerType)
            }
            Map::class.qualifiedName!!, "kotlin.collections.MutableMap" -> {
                val keyType = extract(type.arguments[0].type!!.resolve())
                val valueType = extract(type.arguments[1].type!!.resolve())
                return QPropertyType.MapCollection(keyType, valueType)
            }
            else -> null
        }
    }

    private fun referenceType(type: KSType): QPropertyType? {
        val className = type.toClassName()
        val referencedDeclaration = type.declaration
        if (referencedDeclaration is KSClassDeclaration) {
            return when (referencedDeclaration.classKind) {
                ClassKind.ENUM_CLASS -> QPropertyType.EnumReference(type.toClassName())
                ClassKind.CLASS, ClassKind.INTERFACE -> {
                    val target = models.singleOrNull { it.originalClassName == className }
                    if (target == null) {
                        return null
                    } else {
                        return QPropertyType.ObjectReference(target)
                    }
                }
                else -> null
            }
        } else {
            return null
        }
    }

    private fun throwError(message: String): Nothing {
        error("Error processing ${property.qualifiedName!!.asString()}: $message")
    }
}
