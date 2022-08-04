package ru.frozenpriest.schedule

import org.quartz.Job
import org.quartz.Scheduler
import org.quartz.spi.JobFactory
import org.quartz.spi.TriggerFiredBundle
import ru.frozenpriest.service.ScrapRepository
import kotlin.reflect.jvm.jvmName

class MyJobFactory(private val mangaRepository: ScrapRepository): JobFactory {

    override fun newJob(bundle: TriggerFiredBundle?, scheduler: Scheduler?): Job {
        if (bundle != null) {
            val jobClass = bundle.jobDetail.jobClass
            if (jobClass.name == CheckUpdatesJob::class.jvmName) {
                return CheckUpdatesJob(mangaRepository)
            }
        }
        throw NotImplementedError("Job Factory error")
    }
}