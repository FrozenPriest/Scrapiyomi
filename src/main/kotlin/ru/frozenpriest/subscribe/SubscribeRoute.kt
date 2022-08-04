package ru.frozenpriest.subscribe

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ru.frozenpriest.model.MangaRequest
import ru.frozenpriest.service.ScrapRepository

fun Route.subscribeRoute() {
    val repo by inject<ScrapRepository>()

    route("/subscribe") {
        put() {
            val mangaUrl = call.receive<MangaRequest>()

            repo.addUrl(mangaUrl.url)
        }
    }
}