package net.orekyuu.ktmapper

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

data class Item(val id: Long, val name: String)
data class LineItem(val item: Item, val quantity: Long)
data class Receipt(val id: Long, val createdAt: LocalDateTime, val items: List<LineItem>)

internal class MappingBuilderTest {

    val testData = listOf(
        mapOf("receipt_id" to 1L, "created_at" to LocalDate.of(2019, 5, 5).atStartOfDay(),
            "line_item_id" to 1L, "quantity" to 1L, "item_id" to 1L, "name" to "item1"),
        mapOf("receipt_id" to 1L, "created_at" to LocalDate.of(2019, 5, 5).atStartOfDay(),
            "line_item_id" to 2L, "quantity" to 3L, "item_id" to 2L, "name" to "item2"),
        mapOf("receipt_id" to 2L, "created_at" to LocalDate.of(2019, 5, 6).atStartOfDay(),
            "line_item_id" to 3L, "quantity" to 3L, "item_id" to 1L, "name" to "item1")
    )

    @Test
    fun mappingFlatModel() {
        val testData = listOf(
            mapOf("receipt_id" to 1L, "created_at" to LocalDate.of(2019, 5, 5).atStartOfDay()),
            mapOf("receipt_id" to 2L, "created_at" to LocalDate.of(2019, 5, 6).atStartOfDay())
        )

        val rowMapper = mapping<Map<String, Any>, Receipt> {
            attribute {
                Receipt(it["receipt_id"] as Long, it["created_at"] as LocalDateTime, listOf())
            }
        }

        Assertions.assertThat(rowMapper.toList(testData)).isEqualTo(listOf(
            Receipt(1L, LocalDate.of(2019, 5, 5).atStartOfDay(), listOf()),
            Receipt(2L, LocalDate.of(2019, 5, 6).atStartOfDay(), listOf())
        ))
    }

    @Test
    fun mappingFlatModelWithPK() {
        val testData = listOf(
            mapOf("receipt_id" to 1L, "created_at" to LocalDate.of(2019, 5, 5).atStartOfDay()),
            mapOf("receipt_id" to 2L, "created_at" to LocalDate.of(2019, 5, 6).atStartOfDay())
        )

        val rowMapper = mapping<Map<String, Any>, Receipt> {
            primaryKey {
                it["receipt_id"] as Long
            }

            attribute {
                Receipt(it["receipt_id"] as Long, it["created_at"] as LocalDateTime, listOf())
            }
        }

        Assertions.assertThat(rowMapper.toList(testData)).isEqualTo(listOf(
            Receipt(1L, LocalDate.of(2019, 5, 5).atStartOfDay(), listOf()),
            Receipt(2L, LocalDate.of(2019, 5, 6).atStartOfDay(), listOf())
        ))
    }

    @Test
    fun mappingRelation() {
        val rowMapper = mapping<Map<String, Any>, Receipt> {
            primaryKey { it["receipt_id"] as Long }

            val lineItemRef = hasMany<LineItem> {
                primaryKey { it["line_item_id"] as Long }

                val itemRef = hasOne<Item> {
                    attribute {
                        Item(it["item_id"] as Long, it["name"] as String)
                    }
                }

                attribute {
                    LineItem(findOne(itemRef, it)!!, it["quantity"] as Long)
                }
            }

            attribute {
                Receipt(it["receipt_id"] as Long, it["created_at"] as LocalDateTime, findChild(lineItemRef, it))
            }
        }

        val toList = rowMapper.toList(testData)
        Assertions.assertThat(toList).isEqualTo(listOf(
            Receipt(1L, LocalDate.of(2019, 5, 5).atStartOfDay(),
                listOf(LineItem(Item(1L, "item1"), 1), LineItem(Item(2L, "item2"), 3))),
            Receipt(2L, LocalDate.of(2019, 5, 6).atStartOfDay(),
                listOf(LineItem(Item(1L, "item1"), 3)))
        ))
    }

    @Test
    fun mappingRelationPKNotfound() {
        val rowMapper = mapping<Map<String, Any>, Receipt> {
            val lineItemRef = hasMany<LineItem> {
                val itemRef = hasOne<Item> {
                    attribute {
                        Item(it["item_id"] as Long, it["name"] as String)
                    }
                }

                attribute {
                    LineItem(findOne(itemRef, it)!!, it["quantity"] as Long)
                }
            }

            attribute {
                Receipt(it["receipt_id"] as Long, it["created_at"] as LocalDateTime, findChild(lineItemRef, it))
            }
        }

        Assertions.assertThatThrownBy { rowMapper.toList(testData) }
            .hasMessage("primaryKey block is required.")
            .isInstanceOf(IllegalStateException::class.java)
    }
}