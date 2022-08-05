package ru.frozenpriest.scrapper.source

interface SourceManager {
    fun getSource(id: Long): Source?
}