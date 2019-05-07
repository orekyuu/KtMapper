# KtMapper 
[ ![Download](https://api.bintray.com/packages/orekyuu/KtMapper/net.orekyuu.kt-mapper/images/download.svg?version=0.0.2) ](https://bintray.com/orekyuu/KtMapper/net.orekyuu.kt-mapper/0.0.2/link)   
SQLのJoinした結果をドメインモデルにマッピングするライブラリです。  
DSLを使ってMappingの構造を定義します。

## Example
```kotlin
// Map<String, Any> -> ドメインモデルへの変換

data class Item(val id: Long, val name: String)
data class LineItem(val item: Item, val quantity: Long)
data class Receipt(val id: Long, val createdAt: LocalDateTime, val items: List<LineItem>)

// マッピング定義
val rowMapper = mapping<Map<String, Any>, Receipt> {
    primaryKey { Pair(it["receipt_id"] as Long, it["created_at"] as LocalDateTime) }

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

// mapperを使った変換
val result: List<Receipt> = rowMapper.mappingList(testData)
val result: Set<Receipt> = rowMapper.mappingSet(testData)
val result: Receipt? = rowMapper.firstOrNull(testData)
```