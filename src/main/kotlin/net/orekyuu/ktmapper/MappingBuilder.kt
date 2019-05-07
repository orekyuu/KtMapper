package net.orekyuu.ktmapper

import java.util.*

class Relation<ROW>(val id: RelationReference<*>, val mapping: MappingBuilder<ROW, *>.(Context<ROW>) -> Unit)

open class RelationReference<T>(protected open val id: String)

data class HasOneRelationReference<T>(override val id: String = UUID.randomUUID().toString()) : RelationReference<T>(id)
data class HasManyRelationReference<T>(override val id: String = UUID.randomUUID().toString()) : RelationReference<T>(id)


class MappingBuilder<ROW, RESULT> {

    private var domainFunction: (Context<ROW>.(ROW) -> RESULT)? = null
    private var relations: MutableList<Relation<ROW>> = mutableListOf()
    private var primaryKeyFunc: ((ROW) -> Any)? = null

    fun primaryKey(primaryKeyFunc: (ROW) -> Any) {
        this.primaryKeyFunc = primaryKeyFunc
    }

    fun domain(func: Context<ROW>.(ROW) -> RESULT) {
        domainFunction = func
    }

    fun <CHILD> hasMany(builderFunc: MappingBuilder<ROW, CHILD>.(Context<ROW>) -> Unit): HasManyRelationReference<CHILD> {
        val reference = HasManyRelationReference<CHILD>()

        @Suppress("UNCHECKED_CAST") val relation = Relation(reference, builderFunc as MappingBuilder<ROW, *>.(Context<ROW>) -> Unit)
        relations.add(relation)

        return reference
    }

    fun <CHILD> hasOne(builderFunc: MappingBuilder<ROW, CHILD>.(Context<ROW>) -> Unit): HasOneRelationReference<CHILD> {
        val reference = HasOneRelationReference<CHILD>()

        @Suppress("UNCHECKED_CAST") val relation = Relation(reference, builderFunc as MappingBuilder<ROW, *>.(Context<ROW>) -> Unit)
        relations.add(relation)

        return reference
    }

    internal fun createRowMapper(): RowMapper<ROW, RESULT> {
        return RowMapper(primaryKeyFunc, domainFunction, relations)
    }
}