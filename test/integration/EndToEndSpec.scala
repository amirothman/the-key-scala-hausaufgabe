package integration

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers
import play.api.test._
import services.{FetcherService, ProcessorService, WebSocketBroadcaster}
import models.{BlogPost, WordCountMap}
import sttp.client3.testing.SttpBackendStub
import sttp.client3.Response
import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.concurrent.ExecutionContext.Implicits.global

class EndToEndSpec extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with Matchers with BeforeAndAfterAll {
  implicit val system: ActorSystem = ActorSystem("EndToEndSpec")
  implicit val mat: Materializer = Materializer(system)

  "End-to-End Pipeline" should {
    "fetch posts, process them, and broadcast word counts" in {
      // Initialize services
      val fetcher = new FetcherService()
      val processor = new ProcessorService()
      val broadcaster = new WebSocketBroadcaster()

      // Set up mock WordPress API response
      val mockAPIResponse =
        """
        [
          {
            "id": 1,
            "content": {
              "rendered": "Hello world! Hello again."
            }
          },
          {
            "id": 2,
            "content": {
              "rendered": "Scala is great!"
            }
          }
        ]
        """

      val apiUrl = "http://mock.api"
      val mockBackend = SttpBackendStub.synchronous
        .whenRequestMatchesPartial {
          case req if req.uri.toString == apiUrl =>
            Response.ok(mockAPIResponse)
        }

      // Set up client to receive broadcasts
      val clientPromise = Promise[String]()
      broadcaster.wordCountSource.runForeach(msg => clientPromise.trySuccess(msg))

      // Execute the pipeline
      val posts = fetcher.fetchPosts(apiUrl, Some(mockBackend)).futureValue
      posts must contain theSameElementsAs List(
        BlogPost(1, "Hello world! Hello again."),
        BlogPost(2, "Scala is great!")
      )

      val wordCount = processor.process(posts)
      wordCount.data must contain theSameElementsAs Map(
        "hello" -> 2,
        "world" -> 1,
        "again" -> 1,
        "scala" -> 1,
        "is" -> 1,
        "great" -> 1
      )

      broadcaster.broadcastWordCount(wordCount)

      // Verify the broadcast
      val broadcastResult = Await.result(clientPromise.future, 1.second)
      broadcastResult must include("hello")
      broadcastResult must include("2") // hello appears twice
      broadcastResult must include("world")
      broadcastResult must include("scala")
      broadcastResult must include("great")
    }

    "handle empty API response gracefully" in {
      val fetcher = new FetcherService()
      val processor = new ProcessorService()
      val broadcaster = new WebSocketBroadcaster()

      val mockBackend = SttpBackendStub.synchronous
        .whenRequestMatchesPartial {
          case req if req.uri.toString == "http://mock.api" =>
            Response.ok("[]")
        }

      val clientPromise = Promise[String]()
      broadcaster.wordCountSource.runForeach(msg => clientPromise.trySuccess(msg))

      val posts = fetcher.fetchPosts("http://mock.api", Some(mockBackend)).futureValue
      posts must be(empty)

      val wordCount = processor.process(posts)
      wordCount.data must be(empty)

      broadcaster.broadcastWordCount(wordCount)

      val broadcastResult = Await.result(clientPromise.future, 1.second)
      broadcastResult must be("{}")
    }
  }

  override def afterAll(): Unit = {
    Await.result(system.terminate(), 5.seconds)
  }
}
