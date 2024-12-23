package controllers

import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.{Keep, Source}
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.Json
import services.WebSocketBroadcaster
import models.WordCountMap
import play.api.libs.streams.ActorFlow
import play.api.http.websocket._

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import scala.concurrent.duration._

class WebSocketControllerSpec extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with Matchers {

  implicit val mat: Materializer = app.materializer

  "WebSocketController" should {
    "accept WebSocket connections and send updates" in {
      val broadcaster = app.injector.instanceOf[WebSocketBroadcaster]
      val controller = app.injector.instanceOf[WebSocketController]

      // Create a WebSocket connection
      val wsFlow = controller.wordCountSocket(FakeRequest())
        .futureValue
        .toOption.get

      // Set up message collection
      var receivedMessages = List.empty[String]

      // Create a test source and sink
      val testSource = Source.maybe[Message]
      val testSink = org.apache.pekko.stream.scaladsl.Sink.foreach[Message] {
        case TextMessage(text) => receivedMessages = receivedMessages :+ text
        case _ =>
      }

      // Run the WebSocket flow
      val ((sourceMatValue, _), sinkMatValue) = testSource
        .viaMat(wsFlow)(Keep.both)
        .toMat(testSink)(Keep.both)
        .run()

      // Send test data
      val wordCount = WordCountMap(Map("hello" -> 1, "world" -> 2))
      broadcaster.broadcastWordCount(wordCount)

      // Allow time for message processing
      Thread.sleep(500)

      // Verify received messages
      receivedMessages.size must be(1)
      receivedMessages.head must include("hello")
      receivedMessages.head must include("1")
      receivedMessages.head must include("world")
      receivedMessages.head must include("2")
    }
  }
}
