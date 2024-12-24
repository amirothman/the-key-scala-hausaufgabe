package services

import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.testkit.{TestKit, TestProbe}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import play.api.Configuration
import org.slf4j.Logger
import scala.concurrent.ExecutionContext

class PipelineSchedulerSpec
    extends TestKit(ActorSystem("PipelineSchedulerSpec"))
    with AnyWordSpecLike
    with MockitoSugar
    with BeforeAndAfterAll {

  implicit val ec: ExecutionContext = system.dispatcher

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "PipelineScheduler" should {
    "schedule the pipeline to run periodically" in {
      val mockBlogPipeline = mock[BlogProcessingPipeline]
      val mockConfig = mock[Configuration]
      val mockLogger = mock[Logger]

      when(mockConfig.get[String]("wordpress.api.url"))
        .thenReturn("https://thekey.academy/wp-json/wp/v2/posts")
      when(mockConfig.get[Int]("scheduler.interval"))
        .thenReturn(1) // Set a short interval for the test

      // Configure the mock to return a successful Future
      when(mockBlogPipeline.execute(any[String])).thenReturn(Future.successful(()))

      // Create an instance of PipelineScheduler
      val scheduler =
        new PipelineScheduler(mockBlogPipeline, mockConfig, system)

      // Use a TestProbe to wait for the scheduled task to execute
      val probe = TestProbe()
      probe.awaitAssert(
        {
          verify(mockBlogPipeline, atLeastOnce())
            .execute("https://thekey.academy/wp-json/wp/v2/posts")
        },
        5.seconds,
        1.second
      )
    }
  }
}
