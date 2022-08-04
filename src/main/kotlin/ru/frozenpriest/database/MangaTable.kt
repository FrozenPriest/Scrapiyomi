package ru.frozenpriest.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object MangaTable : IntIdTable("manga", "manga_id") {
    val name: Column<String> = varchar("name", 20)

}

class Manga(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Manga>(MangaTable)

    var name by MangaTable.name
}