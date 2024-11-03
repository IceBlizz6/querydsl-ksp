package iceblizz6.querydsl.ksp

import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.ClassName

class QueryModel(
    val originalClassName: ClassName,
    val typeParameterCount: Int,
    val className: ClassName,
    val type: Type,
    val originatingFile: KSFile
) {
    var superclass: QueryModel? = null

    val properties = mutableListOf<QProperty>()

    enum class Type {
        ENTITY,
        EMBEDDABLE,
        SUPERCLASS
    }
}
