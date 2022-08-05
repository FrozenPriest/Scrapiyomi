package ru.frozenpriest.model


/**
 * Model for a manga given by a source
 */
data class MangaInfo(
    val key: String,
    val title: String,
    val artist: String = "",
    val author: String = "",
    val description: String = "",
    val genres: List<String> = emptyList(),
    val status: Int = UNKNOWN,
    val cover: String = ""
) {

    companion object {
        const val UNKNOWN = 0
        const val ONGOING = 1
        const val COMPLETED = 2
        const val LICENSED = 3
        const val PUBLISHING_FINISHED = 4
        const val CANCELLED = 5
    }
}

fun MangaInfo.toLibraryManga(source: Long): LibraryManga {
    return LibraryManga(
        url = key,
        title = title,
        artist = artist,
        author = author,
        description = description,
        genres = genres,
        status = status,
        coverUrl = cover,
        source = source
    )
}