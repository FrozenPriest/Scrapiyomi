package ru.frozenpriest

import io.ktor.server.application.*
import org.koin.ktor.ext.inject

class AppConfig {
    lateinit var jwtConfig: JwtConfig
    lateinit var databaseConfig: DatabaseConfig
}

fun Application.initConfig() {
    val appConfig by inject<AppConfig>()

    val jwtConf = environment.config.config("ktor.jwt")
    appConfig.jwtConfig = JwtConfig(
        secret = jwtConf.property("secret").getString(),
        realm = jwtConf.property("realm").getString()
    )

    val dbConf = environment.config.config("ktor.database")
    appConfig.databaseConfig = DatabaseConfig(
        jdbcUrl = dbConf.property("jdbcUrl").getString(),
        username = dbConf.property("username").getString(),
        password = dbConf.property("password").getString(),
        driver = dbConf.property("driver").getString()
    )
}

data class JwtConfig (
    val secret: String,
    val realm: String
)

data class DatabaseConfig (
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val driver: String
)