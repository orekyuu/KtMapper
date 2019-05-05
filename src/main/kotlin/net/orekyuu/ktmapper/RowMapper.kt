package net.orekyuu.ktmapper

class GroupingRows<ROW>(val relation: Relation<ROW>, val values:  Map<Any, List<ROW>>) {

}

class Context<ROW>(
    private val primaryKeyFunc: ((ROW) -> Any)?,
    hasManyRelations: MutableList<Relation<ROW>>, rows: List<ROW>
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

    @Suppress("UNCHECKED_CAST")
    fun <T> findChild(reference: RelationReference<T>, row: ROW): List<T> {
        val groupingRows = relationMap[reference] ?: throw IllegalArgumentException()
        val relation = groupingRows.relation
        checkPrimaryKeyFuncValidation()

        val parentKeyFunc = primaryKeyFunc!!(row)
        val rawData = groupingRows.values[parentKeyFunc] ?: listOf()

        val builder = MappingBuilder<ROW, Any>()
        relation.mapping(builder, this)
        val rowMapper = builder.createRowMapper()

        return rowMapper.toList(rawData) as List<T>
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> findOne(reference: RelationReference<T>, row: ROW): T? {
        return findChild(reference, row).firstOrNull()
    }

    private fun checkPrimaryKeyFuncValidation() {
        if (relationMap.isNotEmpty() && primaryKeyFunc == null) {
            throw IllegalStateException("primaryKey block is required.")
        }
    }
}


class RowMapper<ROW, RESULT> internal constructor(
    private val primaryKeyFunc: ((ROW) -> Any)?,
    private val fieldFunction: (Context<ROW>.(ROW) -> RESULT)?,
    private val hasManyRelations: MutableList<Relation<ROW>>
) {

    fun toList(rows: List<ROW>): List<RESULT> {

        val context = Context(primaryKeyFunc, hasManyRelations, rows)
        val list = primaryKeyFunc?.let { rows.distinctBy(it) } ?: rows

        @Suppress("UNCHECKED_CAST")
        return list.map { row ->
            val result = fieldFunction?.let { context.it(row) }
            result
        }.filter { it != null } as List<RESULT>
    }
}