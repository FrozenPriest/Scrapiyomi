package ru.frozenpriest

import org.koin.dsl.module
import ru.frozenpriest.service.ScrapRepository
import ru.frozenpriest.service.ScrapRepositoryImpl

val appModule = module {
    single { AppConfig() }
    single<ScrapRepository> { ScrapRepositoryImpl() }
}