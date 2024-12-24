package services

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.scalatestplus.play.PlaySpec
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers
import models.WordCountMap
import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.concurrent.ExecutionContext.Implicits.global

class WebSocketBroadcasterSpec extends PlaySpec with ScalaFutures with Matchers with BeforeAndAfterAll {
  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = Materializer(system)

  "WebSocketBroadcaster" should {
    "broadcast messages to all connected clients" in {
      val broadcaster = new WebSocketBroadcaster()
      val wordCount = WordCountMap(Map("hello" -> 1, "world" -> 2))

      // Create two clients
      val client1Promise = Promise[String]()
      val client2Promise = Promise[String]()

      // Subscribe clients
      broadcaster.wordCountSource.runForeach(msg => client1Promise.trySuccess(msg))
      broadcaster.wordCountSource.runForeach(msg => client2Promise.trySuccess(msg))

      // Broadcast message
      broadcaster.broadcastWordCount(wordCount)

      // Wait for both clients to receive the message
      val result1 = Await.result(client1Promise.future, 1.second)
      val result2 = Await.result(client2Promise.future, 1.second)

      // Both clients should receive the same message
      result1 must be(result2)
      result1 must include("hello")
      result1 must include("1")
      result1 must include("world")
      result1 must include("2")
    }

    "handle multiple broadcasts" in {
      val broadcaster = new WebSocketBroadcaster()
      val messages = scala.collection.mutable.ListBuffer.empty[String]

      broadcaster.wordCountSource.runForeach(msg => messages += msg)

      // Send multiple word counts
      broadcaster.broadcastWordCount(WordCountMap(Map("first" -> 1)))
      broadcaster.broadcastWordCount(WordCountMap(Map("second" -> 2)))

      // Allow time for processing
      Thread.sleep(1000)

      messages.size must be(2)
      messages.head must include("first")
      messages.last must include("second")
    }
  }

  override def afterAll(): Unit = {
    Await.result(system.terminate(), 5.seconds)
  }
}
