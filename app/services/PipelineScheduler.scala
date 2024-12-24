package services

import javax.inject._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import play.api.Configuration
import play.api.Logging

@Singleton
class PipelineScheduler @Inject() (
    blogPipeline: BlogProcessingPipeline,
    config: Configuration,
    actorSystem: org.apache.pekko.actor.ActorSystem
)(implicit ec: ExecutionContext)
    extends Logging {

  // Read the API URL and interval from configuration
  private val apiUrl = config.get[String]("wordpress.api.url")
  private val interval = config.get[Int]("scheduler.interval").seconds

  logger.info(s"Starting PipelineScheduler with interval: $interval seconds")

  // Schedule the pipeline to run periodically
  actorSystem.scheduler.scheduleWithFixedDelay(initialDelay = 0.seconds, delay = interval) { () =>
    logger.info(s"Triggering blogPipeline execution at ${java.time.Instant.now}")
    blogPipeline.execute(apiUrl).recover {
      case ex: Exception =>
        logger.error("Pipeline execution failed", ex)
    }
  }
}
