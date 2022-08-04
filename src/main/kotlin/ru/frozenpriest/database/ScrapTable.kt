package ru.frozenpriest.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object ScrapTable : IntIdTable("scraps", "manga_id") {
    val url: Column<String> = varchar("url", 255)
}

class ScrapItem(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ScrapItem>(ScrapTable)
    var url by ScrapTable.url
}