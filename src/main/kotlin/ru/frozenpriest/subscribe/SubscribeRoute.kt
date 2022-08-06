package ru.frozenpriest.subscribe

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ru.frozenpriest.data.ScrapRepository

fun Route.subscribeRoute() {
    val repo by inject<ScrapRepository>()

    route("/subscribe") {
        post {
            call.request.queryParameters["url"]?.let {mangaUrl ->
                println("Got new manga: $mangaUrl")
                repo.addUrl(mangaUrl)
                call.respond(HttpStatusCode.Created, "Url is saved.")
                return@post
            }
            call.respond(HttpStatusCode.BadRequest, "Url is required")
        }
    }
}