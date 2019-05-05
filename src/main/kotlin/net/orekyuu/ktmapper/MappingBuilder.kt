package net.orekyuu.ktmapper

import java.util.*

class Relation<ROW>(val id: RelationReference<*>, val mapping: MappingBuilder<ROW, *>.(Context<ROW>) -> Unit) {

}

data class RelationReference<T>(private val id: String = UUID.randomUUID().toString())

class MappingBuilder<ROW, RESULT> {

    private var attributeFunction: (Context<ROW>.(ROW) -> RESULT)? = null
    private var hasManyRelations: MutableList<Relation<ROW>> = mutableListOf()
    private var primaryKeyFunc: ((ROW) -> Any)? = null

    fun primaryKey(primaryKeyFunc: (ROW) -> Any) {
        this.primaryKeyFunc = primaryKeyFunc
    }

    fun attribute(func: Context<ROW>.(ROW) -> RESULT) {
        attributeFunction = func
    }

    fun <CHILD> hasMany(builderFunc: MappingBuilder<ROW, CHILD>.(Context<ROW>) -> Unit): RelationReference<CHILD> {
        val reference = RelationReference<CHILD>()

        @Suppress("UNCHECKED_CAST") val relation = Relation(reference, builderFunc as MappingBuilder<ROW, *>.(Context<ROW>) -> Unit)
        hasManyRelations.add(relation)

        return reference
    }

    fun <CHILD> hasOne(builderFunc: MappingBuilder<ROW, CHILD>.(Context<ROW>) -> Unit): RelationReference<CHILD> {
        return hasMany(builderFunc)
    }

    internal fun createRowMapper(): RowMapper<ROW, RESULT> {
        return RowMapper(primaryKeyFunc, attributeFunction, hasManyRelations)
    }
}