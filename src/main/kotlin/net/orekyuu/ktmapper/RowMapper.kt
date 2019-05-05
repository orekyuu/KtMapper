package net.orekyuu.ktmapper

class Context<ROW>() {

}


class RowMapper<ROW, RESULT> internal constructor(
    private val fieldFunction: (Context<ROW>.(ROW) -> RESULT)?,
    private val hasManyRelations: MutableList<Relation<ROW>>
) {

    fun toList(rows: List<ROW>): List<RESULT> {
        val context = Context<ROW>()
        val list = rows.map { row ->
            val result = fieldFunction?.let { context.it(row) }
            result
        }.filter { it != null } as List<RESULT>
        return list
    }
}