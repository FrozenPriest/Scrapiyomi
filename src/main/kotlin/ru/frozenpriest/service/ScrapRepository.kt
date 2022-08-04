package ru.frozenpriest.service

import org.jetbrains.exposed.sql.transactions.transaction
import ru.frozenpriest.database.ScrapItem

interface ScrapRepository {
    fun addUrl(url: String): Int
    fun getUrls(): List<String>
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

}