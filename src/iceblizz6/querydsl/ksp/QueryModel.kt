package iceblizz6.querydsl.ksp

import com.squareup.kotlinpoet.ClassName

class QueryModel(
    val originalClassName: ClassName,
    val typeParameters: List<ClassName>,
    val className: ClassName,
    val type: Type
) {
    var superclass: QueryModel? = null

    val properties = mutableListOf<QProperty>()

    enum class Type {
        ENTITY,
        EMBEDDABLE,
        SUPERCLASS
    }
}
