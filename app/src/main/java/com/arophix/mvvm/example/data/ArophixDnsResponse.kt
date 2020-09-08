package com.arophix.mvvm.example.data

import androidx.room.*

@Entity(tableName = "arophix_button_texts")
data class ButtonText(
        @PrimaryKey @ColumnInfo(name = "id") val id: String,
        @ColumnInfo(name = "button_text") val text: String = "Another text to be handled on client side." // this is a weird information from server ...
) {
    override fun toString() = id
}

@Entity(
        tableName = "dnslocations",
//        foreignKeys = [
//            ForeignKey(entity = ArophixDnsResponse::class, parentColumns = ["id"], childColumns = ["response_id"])
//        ],
        indices = [Index("name")]
)
data class DnsLocation(
        @PrimaryKey @ColumnInfo(name = "name") val name: String,
        @ColumnInfo(name = "sort_order") val sortOrder: String,
        @ColumnInfo(name = "icon_id") val iconId: String,
        @ColumnInfo(name = "icon_base64_code") var iconBase64Code: String,
        @ColumnInfo(name = "server_list") val serverList: String, // list of IPs separated by comma, e.g. "64.120.99.235,173.234.147.130"

        @ColumnInfo(name = "response_id") val responseId: String
) {
    override fun toString() = name
}

data class ArophixDnsResponse (
        @Embedded
        val buttonText: ButtonText,

        @Relation(parentColumn = "id", entityColumn = "response_id")
        var dnsLocations: List<DnsLocation> = emptyList()
)
