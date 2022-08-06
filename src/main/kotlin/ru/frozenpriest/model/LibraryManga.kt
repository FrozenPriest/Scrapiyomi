package ru.frozenpriest.model

import ru.frozenpriest.data.database.MangaInTable

data class LibraryManga(
    val source: Long,
    val title: String,
    val url: String,
    val artist: String = "",
    val author: String = "",
    val description: String = "",
    val genres: List<String>? = emptyList(),
    val status: Int = MangaInfo.UNKNOWN,
    val coverUrl: String = "",
    val coverLocal: String = "",
    val id: Long = 0,
    val lastUpdate: Long? = null
)

fun LibraryManga.toMangaInfo(): MangaInfo {
    return MangaInfo(
        artist = this.artist ?: "",
        author = this.author ?: "",
        cover = this.coverUrl ?: "",
        description = this.description ?: "",
        genres = this.genres ?: emptyList(),
        key = this.url,
        status = this.status,
        title = this.title,
    )
}

fun MangaInTable.toLibrary(): LibraryManga {
    return LibraryManga(
        source = this.mangaSource,
        title = this.title,
        url = this.mangaUrl,
        artist = this.artist,
        author = this.author,
        description = this.description,
        genres = this.genres.split(","),
        status = this.status,
        coverUrl = this.coverUrl,
        id = this.id.value,
    )
}