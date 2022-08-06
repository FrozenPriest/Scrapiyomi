package ru.frozenpriest.scrapper

import ru.frozenpriest.model.Chapter
import ru.frozenpriest.model.LibraryManga
import ru.frozenpriest.model.SourceChapter
import ru.frozenpriest.scrapper.source.Source

interface DownloadManager {
    fun downloadChapters(manga: LibraryManga, chapters: List<Chapter>, autoStart: Boolean)
    fun isSourceChapterDownloaded(chapter: Chapter, manga: LibraryManga): Boolean
    fun renameChapter(source: Source, manga: LibraryManga, dbChapter: Chapter, sourceChapter: SourceChapter)
}