package ru.frozenpriest.database

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object ChapterTable : LongIdTable("manga", "id") {

}

class ChapterInTable(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<MangaInTable>(MangaTable)

}