package ru.frozenpriest.scrapper

import ru.frozenpriest.model.Chapter
import ru.frozenpriest.model.LibraryManga

interface DownloadManager {
    fun downloadChapters(manga: LibraryManga, chapters: List<Chapter>, autoStart: Boolean)
}