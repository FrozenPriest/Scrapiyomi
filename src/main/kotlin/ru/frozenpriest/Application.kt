package ru.frozenpriest

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.SLF4JLogger
import org.quartz.spi.JobFactory
import ru.frozenpriest.database.MangaTable
import ru.frozenpriest.database.ScrapTable
import ru.frozenpriest.schedule.JobSchedulerManager
import ru.frozenpriest.schedule.startJob
import ru.frozenpriest.subscribe.subscribeRoute

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.initDB() {
    val appConfig by inject<AppConfig>()

    val config = HikariConfig()
    config.jdbcUrl = appConfig.databaseConfig.jdbcUrl
    config.username = appConfig.databaseConfig.username
    config.password = appConfig.databaseConfig.password
    config.driverClassName = appConfig.databaseConfig.driver

    Database.connect(HikariDataSource(config))
    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            MangaTable, ScrapTable
        )
    }
}

fun Application.module() {

    install(Koin) {
        SLF4JLogger()
        modules(appModule)
    }

    initConfig()
    initDB()

    install(ContentNegotiation) {
        json()
    }

    val jobSchedulerManager by inject<JobSchedulerManager>()
    val jobFactory by inject<JobFactory>()
    jobSchedulerManager.startScheduler()
    jobSchedulerManager.scheduler.setJobFactory(jobFactory)
    startJob()

    routing {
        route("/"){
            get {
                call.respond("Hello world!!")
            }
        }
        subscribeRoute()
    }
}