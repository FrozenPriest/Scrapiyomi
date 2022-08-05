package ru.frozenpriest.scrapper

import kotlinx.coroutines.*
import ru.frozenpriest.model.*
import ru.frozenpriest.scrapper.source.ChapterSourceSync
import ru.frozenpriest.scrapper.source.SourceManager
import ru.frozenpriest.service.ScrapRepository

interface MangaUpdater {
    suspend fun updateAllManga()
    suspend fun updateMangaInSource(source: Long): Boolean
    suspend fun updateMangaChapters(manga: LibraryManga): Boolean


    class Base(
        private val sourceManager: SourceManager,
        private val scrapRepository: ScrapRepository,
        private val listener: UpdateListener?,
        private val chapterSyncer: ChapterSourceSync,
        private val downloadManager: DownloadManager
    ) : MangaUpdater {
        private val mangaToUpdateMap = mutableMapOf<Long, List<LibraryManga>>()
        private val newUpdates = mutableMapOf<LibraryManga, Array<Chapter>>()


        override suspend fun updateAllManga() {
            val mangaToUpdate = scrapRepository.getManga()
            mangaToUpdateMap.putAll(mangaToUpdate.groupBy { it.source })

            coroutineScope {
                val list = mangaToUpdateMap.keys.map { source ->
                    async {
                        updateMangaInSource(source)
                    }
                }
                list.awaitAll()
                finishUpdates()
            }
        }

        private suspend fun finishUpdates() {
            if (newUpdates.isNotEmpty()) {
                updateDetails(newUpdates.keys.toList())
                //DownloadService.start(this)
                newUpdates.clear()
            }
        }

        override suspend fun updateMangaInSource(source: Long): Boolean {
            if (mangaToUpdateMap[source] == null) return false
            var count = 0
            var hasDownloads = false
            while (count < mangaToUpdateMap[source]!!.size) {
                val manga = mangaToUpdateMap[source]!![count]
                if (updateMangaChapters(manga)) {
                    hasDownloads = true
                }
                count++
            }
            mangaToUpdateMap[source] = emptyList()
            return hasDownloads
        }

        /**
         * Returns true if update is successful
         */
        override suspend fun updateMangaChapters(manga: LibraryManga): Boolean {
            try {
                val source = sourceManager.getSource(manga.source) ?: return false

                val fetchedChapters = withContext(Dispatchers.IO) {
                    source.getChapterList(manga.toMangaInfo()).map { it.toSChapter() }
                }
                if (fetchedChapters.isNotEmpty()) {
                    val newChapters =
                        chapterSyncer.syncChaptersWithSource(scrapRepository, fetchedChapters, manga, source)
                    if (newChapters.first.isNotEmpty()) {
                        downloadChapters(manga, newChapters.first.sortedBy { it.chapter_number })
                        newUpdates[manga] =
                            newChapters.first.sortedBy { it.chapter_number }.toTypedArray()
                    }
                    if (newChapters.first.size + newChapters.second.size > 0) listener?.onUpdateManga(
                        manga,
                    )
                }
                return true
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    println("Failed updating: ${manga.title}: $e")
                }
                return false
            }
        }

        private fun downloadChapters(manga: LibraryManga, chapters: List<Chapter>) {
            // We don't want to start downloading while the library is updating, because websites
            // may don't like it and they could ban the user.
            downloadManager.downloadChapters(manga, chapters, false)
        }

        /**
         * Method that updates the details of the given list of manga. It's called in a background
         * thread, so it's safe to do heavy operations or network calls here.
         *
         * @param mangaToUpdate the list to update
         */
        suspend fun updateDetails(mangaToUpdate: List<LibraryManga>) = coroutineScope {
            // Initialize the variables holding the progress of the updates.
            val asyncList = mangaToUpdate.groupBy { it.source }.values.map { list ->
                async {
                    list.forEach { manga ->
                        val source = sourceManager.getSource(manga.source) ?: return@async
                        val networkManga = source.getMangaDetails(manga.toMangaInfo())?.toLibraryManga(source.id)

                        if (networkManga != null) {
                            if (manga.coverUrl != networkManga.coverUrl) {
                                //todo save thumbnail
                            }
                            scrapRepository.insertManga(networkManga)
                        }
                    }
                }
            }
            asyncList.awaitAll()
        }
    }
}

interface UpdateListener {
    fun onUpdateManga(manga: LibraryManga)
}

