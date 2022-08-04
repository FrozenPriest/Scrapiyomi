package ru.frozenpriest

import org.koin.dsl.module
import org.quartz.spi.JobFactory
import ru.frozenpriest.schedule.JobSchedulerManager
import ru.frozenpriest.schedule.MyJobFactory
import ru.frozenpriest.service.ScrapRepository
import ru.frozenpriest.service.ScrapRepositoryImpl

val appModule = module {
    single { AppConfig() }
    single<ScrapRepository> { ScrapRepositoryImpl() }
    single { JobSchedulerManager(get()) }
    single<JobFactory> { MyJobFactory(get()) }
}