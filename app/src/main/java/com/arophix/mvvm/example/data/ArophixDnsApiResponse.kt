package com.arophix.mvvm.example.data

import org.simpleframework.xml.*
import java.util.*

/**
 * https://medium.com/@Mak95/parsing-xml-data-via-retrofit-2-x-using-kotlin-3ce007d00cf8
 * Data class that represents an xml format of location information response got from Arophix.
 */
@Root(strict = false)
data class ArophixDnsApiResponse(

    // Success response
    @field: Element(name = "icons", required = false)
    var xmlIcons: XmlIcons? = null,

    @field: Element(name = "locations", required = false)
    var xmlLocations: XmlLocations? = null,

    @field: Element(name = "button_text", required = false)
    var buttonText: String? = null,

    // This is uuid to identify each unique server response.
    val responseId: String = UUID.randomUUID().toString()
)

fun ArophixDnsApiResponse.toDnsLocationList(): List<DnsLocation> {

    val locationList = mutableListOf<DnsLocation>()

    xmlLocations!!.xmlLocationList!!.forEach {
        locationList.add(DnsLocation(
                it.name!!,
                it.sortOrder!!,
                it.iconId!!,
                "",
                it.serverListToString(),
                responseId))
    }

    locationList.forEach{
        it.iconBase64Code = xmlIcons!!.xmlIconList!!.filter {
            xmlIcon -> xmlIcon.id == it.iconId
        }[0].iconBase64!!
    }

    return locationList
}

// Since the mock sever API will always return the same XML,
// append a randomly generated number to indicate that the data is really from a new request.
fun ArophixDnsApiResponse.toButtonText(): ButtonText {
    return ButtonText(responseId, buttonText!!)
}

@Root(name = "icons", strict = false)
data class XmlIcons @JvmOverloads constructor(
    @field: ElementList(inline = true, required = false)
    var xmlIconList: List<XmlIcon>? = null
)

@Root(name = "icon", strict = false)
data class XmlIcon @JvmOverloads constructor (
    @field: Attribute(name = "id")
    var id: String? = null,
    @field: Text
    var iconBase64: String? = null
)

@Root(name = "locations", strict = false)
data class XmlLocations @JvmOverloads constructor(
    @field: ElementList(inline = true)
    var xmlLocationList: List<XmlLocation>? = null
)

@Root(name = "location", strict = false)
data class XmlLocation @JvmOverloads constructor(
    @field: Attribute(name = "name")
    var name: String? = null,
    @field: Attribute(name = "sort_order")
    var sortOrder: String? = null,
    @field: Attribute(name = "icon_id")
    var iconId: String ?= null,

    @field: ElementList(inline = true)
    var xmlServerList: List<XmlServer>? = null
) {
    fun serverListToString() : String = xmlServerList!!.joinToString(separator = ",")
}

@Root(name = "server", strict = false)
data class XmlServer @JvmOverloads constructor(
    @field: Attribute(name = "ip")
    var ip: String? = ""
){
    override fun toString(): String = ip!!
}
