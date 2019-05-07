package net.orekyuu.ktmapper

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class RowMapperTest {
    val rowMapper = mapping<Map<String, Any>, Receipt> {
        primaryKey { it["receipt_id"] as Long }

        val lineItemRef = hasMany<LineItem> {
            primaryKey { it["line_item_id"] as Long }

            val itemRef = hasOne<Item> {
                domain {
                    Item(it["item_id"] as Long, it["name"] as String)
                }
            }

            domain {
                LineItem(findOne(itemRef, it)!!, it["quantity"] as Long)
            }
        }

        domain {
            Receipt(it["receipt_id"] as Long, it["created_at"] as LocalDateTime, findList(lineItemRef, it))
        }
    }

    val testData = listOf(
        mapOf("receipt_id" to 1L, "created_at" to LocalDate.of(2019, 5, 5).atStartOfDay(),
            "line_item_id" to 1L, "quantity" to 1L, "item_id" to 1L, "name" to "item1"),
        mapOf("receipt_id" to 1L, "created_at" to LocalDate.of(2019, 5, 5).atStartOfDay(),
            "line_item_id" to 2L, "quantity" to 3L, "item_id" to 2L, "name" to "item2"),
        mapOf("receipt_id" to 2L, "created_at" to LocalDate.of(2019, 5, 6).atStartOfDay(),
            "line_item_id" to 3L, "quantity" to 3L, "item_id" to 1L, "name" to "item1")
    )

    @Test
    fun list() {
        val result = rowMapper.mappingList(testData)
        Assertions.assertThat(result).isEqualTo(listOf(
            Receipt(1L, LocalDate.of(2019, 5, 5).atStartOfDay(),
                listOf(LineItem(Item(1L, "item1"), 1), LineItem(Item(2L, "item2"), 3))),
            Receipt(2L, LocalDate.of(2019, 5, 6).atStartOfDay(),
                listOf(LineItem(Item(1L, "item1"), 3)))
        ))
    }

    @Test
    fun set() {
        val result = rowMapper.mappingSet(testData)
        Assertions.assertThat(result).isEqualTo(setOf(
            Receipt(1L, LocalDate.of(2019, 5, 5).atStartOfDay(),
                listOf(LineItem(Item(1L, "item1"), 1), LineItem(Item(2L, "item2"), 3))),
            Receipt(2L, LocalDate.of(2019, 5, 6).atStartOfDay(),
                listOf(LineItem(Item(1L, "item1"), 3)))
        ))
    }

    @Test
    fun first() {
        val result = rowMapper.firstOrNull(testData)
        Assertions.assertThat(result).isEqualTo(Receipt(1L, LocalDate.of(2019, 5, 5).atStartOfDay(),
            listOf(LineItem(Item(1L, "item1"), 1), LineItem(Item(2L, "item2"), 3))))

        val emptyResult = rowMapper.firstOrNull(listOf())
        Assertions.assertThat(emptyResult).isNull()
    }
}