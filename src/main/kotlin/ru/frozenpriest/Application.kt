package ru.frozenpriest

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import ru.frozenpriest.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureSerialization()
        configureMonitoring()
        configureHTTP()
        configureRouting()
    }.start(wait = true)
}
