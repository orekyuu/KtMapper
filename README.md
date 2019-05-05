# KtMapper
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
    // リレーションのマッピングには主キーの宣言が必要
    // itにはDBから持ってきた行を表すMap<String, Any>が渡されている
    primaryKey { it["receipt_id"] as Long }

    // hasManyで1-*のリレーションができる
    val lineItemRef = hasMany<LineItem> {
        primaryKey { it["line_item_id"] as Long }
        // ネストしたリレーションのマッピング
        // 1-0..1はhasOneで宣言する
        val itemRef = hasOne<Item> {
            attribute {
                Item(it["item_id"] as Long, it["name"] as String)
            }
        }

        // 行を処理してドメインモデルを返す
        attribute {
            LineItem(findOne(itemRef, it)!!, it["quantity"] as Long)
        }
    }

    attribute {
        // 関連を持って来るにはhasManyやhasOneの戻り値のリファレンスをfindChildに渡す
        Receipt(it["receipt_id"] as Long, it["created_at"] as LocalDateTime, findChild(lineItemRef, it))
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
val result: List<Receipt> = rowMapper.toList(testData)
```