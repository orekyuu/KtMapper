package net.orekyuu.ktmapper

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

data class Item(val id: Long, val name: String)
data class LineItem(val item: Item, val quantity: Long)
data class Receipt(val id: Long, val createdAt: LocalDateTime, val items: List<LineItem>)

internal class MappingBuilderTest {

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
}