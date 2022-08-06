package ru.frozenpriest.data

import org.jetbrains.exposed.sql.transactions.transaction
import ru.frozenpriest.data.database.ChapterTable
import ru.frozenpriest.data.database.MangaInTable
import ru.frozenpriest.data.database.ScrapItem
import ru.frozenpriest.data.database.util.batchUpsert
import ru.frozenpriest.model.Chapter
import ru.frozenpriest.model.LibraryManga
import ru.frozenpriest.model.toLibrary

interface ScrapRepository {
    fun addUrl(url: String): Int
    fun getUrls(): List<String>

    fun getManga(): List<LibraryManga>
    fun insertManga(manga: LibraryManga)
    fun getChapters(manga: LibraryManga): List<Chapter>
    fun updateLastUpdated(manga: LibraryManga, date: Long)
    fun insertChapters(chapters: List<Chapter>): Map<String, Long>
}

class ScrapRepositoryImpl() : ScrapRepository {
    override fun addUrl(url: String): Int {
        return transaction {
            ScrapItem.new {
                this.url = url
            }.id.value
        }
    }

    override fun getUrls(): List<String> {
        return transaction {
            ScrapItem.all().map { it.url }
        }
    }

    override fun getManga(): List<LibraryManga> {
        return transaction {
            MangaInTable.all().map {
                it.toLibrary()
            }
        }
    }

    override fun insertManga(manga: LibraryManga) {
        return transaction {
            val mangaInTable = MangaInTable.findById(manga.id)
            if (mangaInTable == null) {
                MangaInTable.new {
                    updateManga(manga)
                }
            } else {
                mangaInTable.apply {
                    updateManga(manga)
                }
            }
        }
    }

    override fun getChapters(manga: LibraryManga): List<Chapter> {
        TODO("Not yet implemented")
    }

    override fun updateLastUpdated(manga: LibraryManga, date: Long) {
        return transaction {
            MangaInTable.findById(manga.id)?.apply {
                this.updateAt = date
            }
        }
    }

    override fun insertChapters(chapters: List<Chapter>): Map<String, Long> {
        return transaction {
            ChapterTable.batchUpsert(data = chapters) { table, item ->
                table[manga_id] = item.manga_id
                table[url] = item.url
                table[name] = item.name
                table[scanlator] = item.scanlator
                table[date_fetch] = item.date_fetch
                table[date_upload] = item.date_upload
                table[chapter_number] = item.chapter_number
                table[source_order] = item.source_order
            }.resultedValues?.map {
                it[ChapterTable.url] to it[ChapterTable.id]
            }?.associateBy {
                it.first
            }?.mapValues { it.value.second.value }
                ?: emptyMap()
        }
    }

    private fun MangaInTable.updateManga(manga: LibraryManga) {
        mangaSource = manga.source
        title = manga.title
        mangaUrl = manga.url
        artist = manga.artist
        author = manga.author
        description = manga.description
        genres = manga.genres?.joinToString(",") ?: ""
        status = manga.status
        coverUrl = manga.coverUrl
    }

}