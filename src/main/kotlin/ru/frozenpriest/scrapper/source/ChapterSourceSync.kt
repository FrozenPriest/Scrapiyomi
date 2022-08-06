package ru.frozenpriest.scrapper.source

import ru.frozenpriest.data.ScrapRepository
import ru.frozenpriest.model.Chapter
import ru.frozenpriest.model.LibraryManga
import ru.frozenpriest.model.SourceChapter
import ru.frozenpriest.scrapper.DownloadManager
import java.time.Instant
import java.util.*

class ChapterSourceSync(
    private val downloadManager: DownloadManager,
) {
    /**
     * Helper method for syncing the list of chapters from the source with the ones from the database.
     *
     * @param db the database.
     * @param rawSourceChapters a list of chapters from the source.
     * @param manga the manga of the chapters.
     * @param source the source of the chapters.
     * @return new insertions.
     */
    fun syncChaptersWithSource(
        db: ScrapRepository,
        rawSourceChapters: List<SourceChapter>,
        manga: LibraryManga,
        source: Source,
    ): List<Chapter> {
        if (rawSourceChapters.isEmpty()) {
            throw Exception("No chapters found")
        }

        // Chapters from db.
        val dbChapters = db.getChapters(manga)

        val sourceChapters = rawSourceChapters
            .distinctBy { it.url }
            .mapIndexed { i, SourceChapter ->
                Chapter.create().apply {
                    copyFrom(SourceChapter)
                    manga_id = manga.id
                    source_order = i
                }
            }

        // Chapters from the source not in db.
        val toAdd = mutableListOf<Chapter>()

        // Chapters whose metadata have changed.
        val toChange = mutableListOf<Chapter>()

        for (sourceChapter in sourceChapters) {
            val dbChapter = dbChapters.find { it.url == sourceChapter.url }

            // Add the chapter if not in db already, or update if the metadata changed.
            if (dbChapter == null) {
                toAdd.add(sourceChapter)
            } else {
                // this forces metadata update for the main viewable things in the chapter list
                source.prepareNewChapter(sourceChapter, manga)

                ChapterRecognition.parseChapterNumber(sourceChapter, manga)

                if (shouldUpdateDbChapter(dbChapter, sourceChapter)) {
                    if (dbChapter.name != sourceChapter.name &&
                        downloadManager.isSourceChapterDownloaded(dbChapter, manga)
                    ) {
                        downloadManager.renameChapter(source, manga, dbChapter, sourceChapter)
                    }
                    dbChapter.scanlator = sourceChapter.scanlator
                    dbChapter.name = sourceChapter.name
                    dbChapter.date_upload = sourceChapter.date_upload
                    dbChapter.chapter_number = sourceChapter.chapter_number
                    dbChapter.source_order = sourceChapter.source_order
                    toChange.add(dbChapter)
                }
            }
        }

        // Recognize number for new chapters.
        toAdd.forEach {
            source.prepareNewChapter(it, manga)
            ChapterRecognition.parseChapterNumber(it, manga)
        }


        // Return if there's nothing to add, delete or change, avoid unnecessary db transactions.
        if (toAdd.isEmpty() && toChange.isEmpty()) {
            val newestDate = dbChapters.maxOfOrNull { it.date_upload } ?: 0L
            if (newestDate != 0L && newestDate != manga.lastUpdate) {
                db.updateLastUpdated(manga, newestDate)
            }
            return emptyList()
        }



        if (toAdd.isNotEmpty()) {
            // Set the date fetch for new items in reverse order to allow another sorting method.
            // Sources MUST return the chapters from most to less recent, which is common.
            var now = Date().time

            for (i in toAdd.indices.reversed()) {
                val chapter = toAdd[i]
                chapter.date_fetch = now++
            }
            val chapters = db.insertChapters(toAdd)
            toAdd.forEach { chapter ->
                chapter.id = chapters.getValue(chapter.url)
            }
        }

        if (toChange.isNotEmpty()) {
            db.insertChapters(toChange)
        }

        // Fix order in source. todo NO U, not sure why this is needed
        //db.fixChaptersSourceOrder(sourceChapters)

        // Set this manga as updated since chapters were changed
        val newestChapterDate = db.getChapters(manga).maxOfOrNull { it.date_upload } ?: Instant.now().toEpochMilli()

        db.updateLastUpdated(manga, newestChapterDate)

        return toAdd
    }

    // checks if the chapter in db needs updated
    private fun shouldUpdateDbChapter(dbChapter: Chapter, sourceChapter: Chapter): Boolean {
        return dbChapter.scanlator != sourceChapter.scanlator || dbChapter.name != sourceChapter.name ||
                dbChapter.date_upload != sourceChapter.date_upload ||
                dbChapter.chapter_number != sourceChapter.chapter_number ||
                dbChapter.source_order != sourceChapter.source_order
    }
}