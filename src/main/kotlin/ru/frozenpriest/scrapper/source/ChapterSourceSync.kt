package ru.frozenpriest.scrapper.source

import io.ktor.server.application.*
import org.koin.ktor.ext.inject
import ru.frozenpriest.model.Chapter
import ru.frozenpriest.model.LibraryManga
import ru.frozenpriest.model.LibraryManga
import ru.frozenpriest.model.SourceChapter
import ru.frozenpriest.scrapper.ChapterFilter
import ru.frozenpriest.scrapper.DownloadManager
import ru.frozenpriest.service.ScrapRepository
import java.util.*

class ChapterSourceSync(
    val downloadManager: DownloadManager,
    val chapterFilter: ChapterFilter
) {
    /**
     * Helper method for syncing the list of chapters from the source with the ones from the database.
     *
     * @param db the database.
     * @param rawSourceChapters a list of chapters from the source.
     * @param manga the manga of the chapters.
     * @param source the source of the chapters.
     * @return a pair of new insertions and deletions.
     */
    fun syncChaptersWithSource(
        db: ScrapRepository,
        rawSourceChapters: List<SourceChapter>,
        manga: LibraryManga,
        source: Source,
    ): Pair<List<Chapter>, List<Chapter>> {
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
                        downloadManager.iSourceChapterDownloaded(dbChapter, manga)
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

        // Chapters from the db not in the source.
        val toDelete = dbChapters.filterNot { dbChapter ->
            sourceChapters.any { sourceChapter ->
                dbChapter.url == sourceChapter.url
            }
        }

        // Return if there's nothing to add, delete or change, avoid unnecessary db transactions.
        if (toAdd.isEmpty() && toDelete.isEmpty() && toChange.isEmpty()) {
            val newestDate = dbChapters.maxOfOrNull { it.date_upload } ?: 0L
            if (newestDate != 0L && newestDate != manga.last_update) {
                manga.last_update = newestDate
                db.updateLastUpdated(manga).executeAsBlocking()
            }
            return Pair(emptyList(), emptyList())
        }

        val readded = mutableListOf<Chapter>()

        db.inTransaction {
            val deletedChapterNumbers = TreeSet<Float>()
            val deletedReadChapterNumbers = TreeSet<Float>()
            if (toDelete.isNotEmpty()) {
                for (c in toDelete) {
                    if (c.read) {
                        deletedReadChapterNumbers.add(c.chapter_number)
                    }
                    deletedChapterNumbers.add(c.chapter_number)
                }
                db.deleteChapters(toDelete).executeAsBlocking()
            }

            if (toAdd.isNotEmpty()) {
                // Set the date fetch for new items in reverse order to allow another sorting method.
                // Sources MUST return the chapters from most to less recent, which is common.
                var now = Date().time

                for (i in toAdd.indices.reversed()) {
                    val chapter = toAdd[i]
                    chapter.date_fetch = now++
                    if (chapter.isRecognizedNumber && chapter.chapter_number in deletedChapterNumbers) {
                        // Try to mark already read chapters as read when the source deletes them
                        if (chapter.chapter_number in deletedReadChapterNumbers) {
                            chapter.read = true
                        }
                        // Try to to use the fetch date it originally had to not pollute 'Updates' tab
                        toDelete.filter { it.chapter_number == chapter.chapter_number }
                            .minByOrNull { it.date_fetch }?.let {
                                chapter.date_fetch = it.date_fetch
                            }

                        readded.add(chapter)
                    }
                }
                val chapters = db.insertChapters(toAdd).executeAsBlocking()
                toAdd.forEach { chapter ->
                    chapter.id = chapters.results().getValue(chapter).insertedId()
                }
            }

            if (toChange.isNotEmpty()) {
                db.insertChapters(toChange).executeAsBlocking()
            }

            // Fix order in source.
            db.fixChaptersSourceOrder(sourceChapters).executeAsBlocking()

            // Set this manga as updated since chapters were changed
            val newestChapterDate = db.getChapters(manga).executeAsBlocking()
                .maxOfOrNull { it.date_upload } ?: 0L
            if (newestChapterDate == 0L) {
                if (toAdd.isNotEmpty()) {
                    manga.last_update = Date().time
                }
            } else manga.last_update = newestChapterDate
            db.updateLastUpdated(manga).executeAsBlocking()
        }
        return Pair(
            chapterFilter.filterChaptersByScanlators(toAdd.subtract(readded).toList(), manga),
            toDelete - readded,
        )
    }

    // checks if the chapter in db needs updated
    private fun shouldUpdateDbChapter(dbChapter: Chapter, sourceChapter: Chapter): Boolean {
        return dbChapter.scanlator != sourceChapter.scanlator || dbChapter.name != sourceChapter.name ||
                dbChapter.date_upload != sourceChapter.date_upload ||
                dbChapter.chapter_number != sourceChapter.chapter_number ||
                dbChapter.source_order != sourceChapter.source_order
    }
}