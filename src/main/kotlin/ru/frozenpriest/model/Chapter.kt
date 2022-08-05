package ru.frozenpriest.model

import java.io.Serializable

data class ChapterInfo(
    var key: String, var name: String, var dateUpload: Long = 0, var number: Float = -1f, var scanlator: String = ""
)

open class SourceChapter : Serializable {
    open lateinit var url: String
    open lateinit var name: String
    open var date_upload: Long = 0
    open var chapter_number: Float = -1f
    open var scanlator: String? = null

    fun copyFrom(other: SourceChapter) {
        name = other.name
        url = other.url
        date_upload = other.date_upload
        chapter_number = other.chapter_number
        scanlator = other.scanlator
    }

    fun toChapter(): Chapter {
        return Chapter().apply {
            name = this@SourceChapter.name
            url = this@SourceChapter.url
            date_upload = this@SourceChapter.date_upload
            chapter_number = this@SourceChapter.chapter_number
            scanlator = this@SourceChapter.scanlator
        }
    }

    companion object {
        fun create(): SourceChapter {
            return SourceChapter()
        }
    }
}


fun SourceChapter.toChapterInfo(): ChapterInfo {
    return ChapterInfo(
        dateUpload = this.date_upload,
        key = this.url,
        name = this.name,
        number = this.chapter_number,
        scanlator = this.scanlator ?: "",
    )
}

fun ChapterInfo.toSChapter(): SourceChapter {
    return SourceChapter().apply {
        url = this@toSChapter.key
        name = this@toSChapter.name
        date_upload = this@toSChapter.dateUpload
        chapter_number = this@toSChapter.number
        scanlator = this@toSChapter.scanlator
    }
}

class Chapter : SourceChapter() {
    var id: Long? = null
    var manga_id: Long? = null
    override lateinit var url: String
    override lateinit var name: String
    override var scanlator: String? = null
    var read: Boolean = false
    var bookmark: Boolean = false
    var last_page_read: Int = 0
    var pages_left: Int = 0
    var date_fetch: Long = 0
    override var date_upload: Long = 0
    override var chapter_number: Float = 0f
    var source_order: Int = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val chapter = other as Chapter
        return url == chapter.url
    }

    override fun hashCode(): Int {
        return url.hashCode()
    }

    companion object {
        fun create(): Chapter = Chapter().apply {
            chapter_number = -1f
        }

        fun List<Chapter>.copy(): List<Chapter> {
            return map {
                Chapter().apply {
                    copyFrom(it)
                }
            }
        }
    }

    fun copyFrom(other: Chapter) {
        id = other.id
        manga_id = other.manga_id
        read = other.read
        bookmark = other.bookmark
        last_page_read = other.last_page_read
        pages_left = other.pages_left
        date_fetch = other.date_fetch
        source_order = other.source_order
        copyFrom(other as SourceChapter)
    }
}
