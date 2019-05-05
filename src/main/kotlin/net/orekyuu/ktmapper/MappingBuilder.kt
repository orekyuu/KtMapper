package net.orekyuu.ktmapper

import java.util.*

class Relation<ROW>(val id: RelationReference<*>, val func: Context<ROW>.(ROW) -> Any) {

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

    fun <CHILD> hasMany(func: Context<ROW>.(ROW) -> CHILD): RelationReference<CHILD> {
        val reference = RelationReference<CHILD>()
        @Suppress("UNCHECKED_CAST") val relation = Relation(reference, func as Context<ROW>.(ROW) -> Any)
        hasManyRelations.add(relation)

        return reference
    }

    internal fun createRowMapper(): RowMapper<ROW, RESULT> {
        return RowMapper(primaryKeyFunc, attributeFunction, hasManyRelations)
    }
}