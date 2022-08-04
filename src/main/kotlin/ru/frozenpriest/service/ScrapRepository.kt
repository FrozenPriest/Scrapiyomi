package ru.frozenpriest.service

import org.jetbrains.exposed.sql.transactions.transaction
import ru.frozenpriest.database.ScrapItem

interface ScrapRepository {
    suspend fun addUrl(url: String): Int
}

class ScrapRepositoryImpl() : ScrapRepository {
    override suspend fun addUrl(url: String): Int {
        return transaction {
            ScrapItem.new {
                this.url = url
            }.id.value
        }
    }

}