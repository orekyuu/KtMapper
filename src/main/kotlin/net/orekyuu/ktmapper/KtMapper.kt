package net.orekyuu.ktmapper

fun <ROW, RESULT> mapping(builderFunc: MappingBuilder<ROW, RESULT>.() -> Unit): RowMapper<ROW, RESULT> {
    val builder = MappingBuilder<ROW, RESULT>()
    builder.builderFunc()
    return builder.createRowMapper()
}