package ru.frozenpriest.schedule

import io.ktor.server.application.*
import org.koin.ktor.ext.inject
import org.quartz.*
import ru.frozenpriest.data.ScrapRepository

class CheckUpdatesJob(
    private val mangaRepository: ScrapRepository
) : Job {

    override fun execute(context: JobExecutionContext?) {
        if (context == null) {
            return
        }

        val dataMap = context.jobDetail.jobDataMap

        val name: String? = try {
            dataMap.getString(JOB_MAP_NAME_ID_KEY)
        } catch (e: ClassCastException) {
            null
        }

        if (name != null) {
            println("Checking for updates:")
            val greetingMessage = mangaRepository.getUrls().joinToString(", ")
            println(greetingMessage)
        }
    }

    companion object {
        const val JOB_MAP_NAME_ID_KEY = "name"
        const val WATCH_JOB_GROUP = "WatchJob"
    }
}

fun Application.startJob() {
    val jobSchedulerManager by inject<JobSchedulerManager>()
    val jobId = "chuck-watch-job-for-name-fff"
    val triggerId = "chuck-watch-trigger-for-name-fff"

    // If a job exists, delete it!
    val jobScheduler = jobSchedulerManager.scheduler
    val jobKey = JobKey.jobKey(jobId, CheckUpdatesJob.WATCH_JOB_GROUP)
    jobScheduler.deleteJob(jobKey)

    val job: JobDetail = JobBuilder.newJob(CheckUpdatesJob::class.java)
        .withIdentity(jobId, CheckUpdatesJob.WATCH_JOB_GROUP)
        .usingJobData(CheckUpdatesJob.JOB_MAP_NAME_ID_KEY, "fff")
        .build()

    val trigger: Trigger = TriggerBuilder.newTrigger()
        .withIdentity(triggerId, CheckUpdatesJob.WATCH_JOB_GROUP)
        .withSchedule(
            SimpleScheduleBuilder.simpleSchedule()
                // every minute
                .withIntervalInHours(4)
                .repeatForever()
        )
        .build()

    // Tell quartz to schedule the job using our trigger
    jobSchedulerManager.scheduler.scheduleJob(job, trigger)
}