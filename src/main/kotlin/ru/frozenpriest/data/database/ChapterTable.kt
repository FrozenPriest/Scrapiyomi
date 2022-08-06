package ru.frozenpriest.data.database

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object ChapterTable : LongIdTable("chapter", "id") {
    val manga_id = long("manga_id").nullable()
    val url = varchar("url", 255)
    val name = varchar("name", 255)
    val scanlator = varchar("scanlator", 255).nullable()
    val date_fetch = long("date_fetch")
    val date_upload = long("date_upload")
    val chapter_number = float("chapter_number")
    val source_order = integer("source_order")
}

class ChapterInTable(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<MangaInTable>(MangaTable)

    var manga_id by ChapterTable.manga_id
    var url by ChapterTable.url
    var name by ChapterTable.name
    var scanlator by ChapterTable.scanlator
    var date_fetch by ChapterTable.date_fetch
    var date_upload by ChapterTable.date_upload
    var chapter_number by ChapterTable.chapter_number
    var source_order by ChapterTable.source_order
}