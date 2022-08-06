package ru.frozenpriest.data.database

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column

object MangaTable : LongIdTable("manga", "id") {
    val mangaSource: Column<Long> = long("source")
    val title: Column<String> = varchar("title", 255)
    val mangaUrl: Column<String> = varchar("title", 255)
    val artist: Column<String> = varchar("artist", 255)
    val author: Column<String> = varchar("author", 255)
    val description: Column<String> = varchar("description", 255)
    val genres: Column<String> = varchar("genres", 255).default("")
    val status: Column<Int> = integer("status")
    val coverUrl: Column<String> = varchar("coverUrl", 255)
    val coverLocal: Column<String> = varchar("coverLocal", 255)
    val updateAt: Column<Long> = long("updated_at")
}

class MangaInTable(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<MangaInTable>(MangaTable)

    var mangaSource by MangaTable.mangaSource
    var title by MangaTable.title
    var mangaUrl by MangaTable.mangaUrl
    var artist by MangaTable.artist
    var author by MangaTable.author
    var description by MangaTable.description
    var genres by MangaTable.genres
    var status by MangaTable.status
    var coverUrl by MangaTable.coverUrl
    var coverLocal by MangaTable.coverLocal
    var updateAt by MangaTable.updateAt
}