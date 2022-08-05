package ru.frozenpriest.service

import org.jetbrains.exposed.sql.transactions.transaction
import ru.frozenpriest.database.MangaInTable
import ru.frozenpriest.database.ScrapItem
import ru.frozenpriest.model.Chapter
import ru.frozenpriest.model.LibraryManga
import ru.frozenpriest.model.toLibrary

interface ScrapRepository {
    fun addUrl(url: String): Int
    fun getUrls(): List<String>

    fun getManga(): List<LibraryManga>
    fun insertManga(manga: LibraryManga)
    fun getChapters(manga: LibraryManga): List<Chapter>
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