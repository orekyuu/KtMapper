package net.orekyuu.ktmapper

internal class GroupingRows<ROW>(val relation: Relation<ROW>, val values:  Map<Any, List<ROW>>)

class Context<ROW> internal constructor(
    private val primaryKeyFunc: ((ROW) -> Any)?,
    hasManyRelations: MutableList<Relation<ROW>>,
    rows: Collection<ROW>
) {
    private val relationMap: Map<RelationReference<*>, GroupingRows<ROW>>

    init {
        val map = mutableMapOf<RelationReference<*>, GroupingRows<ROW>>()

        if (hasManyRelations.isNotEmpty() && primaryKeyFunc == null) {
            throw IllegalStateException("primaryKey block is required.")
        }
        for (relation in hasManyRelations) {
            val reference = relation.id
            val values = rows.groupBy(primaryKeyFunc!!)
            map[reference] = GroupingRows(relation, values)
        }
        relationMap = map
    }

    fun <T> findList(reference: HasManyRelationReference<T>, row: ROW): List<T> {
        return findValues(reference, row)
    }

    fun <T> findOne(reference: HasOneRelationReference<T>, row: ROW): T? {
        return findValues(reference, row).firstOrNull()
    }

    private fun <T> findValues(reference: RelationReference<T>, row: ROW): List<T> {
        val groupingRows = relationMap[reference] ?: throw IllegalArgumentException()
        val relation = groupingRows.relation
        checkPrimaryKeyFuncValidation()

        val parentKeyFunc = primaryKeyFunc!!(row)
        val rawData = groupingRows.values[parentKeyFunc] ?: listOf()

        val builder = MappingBuilder<ROW, Any>()
        relation.mapping(builder, this)
        val rowMapper = builder.createRowMapper()

        @Suppress("UNCHECKED_CAST")
        return rowMapper.mappingList(rawData) as List<T>
    }

    private fun checkPrimaryKeyFuncValidation() {
        if (relationMap.isNotEmpty() && primaryKeyFunc == null) {
            throw IllegalStateException("primaryKey block is required.")
        }
    }
}


class RowMapper<ROW, RESULT> internal constructor(
    private val primaryKeyFunc: ((ROW) -> Any)?,
    private val domainFunction: (Context<ROW>.(ROW) -> RESULT)?,
    private val hasManyRelations: MutableList<Relation<ROW>>
) {

    fun mappingList(rows: Collection<ROW>): List<RESULT> {

        val context = Context(primaryKeyFunc, hasManyRelations, rows)
        val list = primaryKeyFunc?.let { rows.distinctBy(it) } ?: rows

        @Suppress("UNCHECKED_CAST")
        return list.map { row ->
            val result = domainFunction?.let { context.it(row) }
            result
        }.filter { it != null } as List<RESULT>
    }

    fun mappingSet(rows: Collection<ROW>): Set<RESULT> {

        val context = Context(primaryKeyFunc, hasManyRelations, rows)
        val list = primaryKeyFunc?.let { rows.distinctBy(it) } ?: rows

        @Suppress("UNCHECKED_CAST")
        return list.map { row ->
            val result = domainFunction?.let { context.it(row) }
            result
        }.filter { it != null }.toSet() as Set<RESULT>
    }

    fun firstOrNull(rows: Collection<ROW>): RESULT? {
        return mappingList(rows).firstOrNull()
    }
}