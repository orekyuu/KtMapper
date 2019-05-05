package net.orekyuu.ktmapper

class Relation<ROW>(val parentKey: (ROW) -> Any, val builder: MappingBuilder<ROW, *>.(ROW) -> Unit) {

}
class MappingBuilder<ROW, RESULT> {

    private var attributeFunction: (Context<ROW>.(ROW) -> RESULT)? = null
    private var hasManyRelations: MutableList<Relation<ROW>> = mutableListOf()

    fun attribute(func: Context<ROW>.(ROW) -> RESULT) {
        attributeFunction = func
    }

    internal fun createRowMapper(): RowMapper<ROW, RESULT> {
        return RowMapper(attributeFunction, hasManyRelations)
    }
}