package services

import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.stream.{Materializer, OverflowStrategy}
import org.apache.pekko.stream.scaladsl.{BroadcastHub, Keep}
import play.api.libs.json.Json
import models.WordCountMap

import javax.inject._

@Singleton
class WebSocketBroadcaster @Inject()(implicit mat: Materializer) {
  private val (hubSink, hubSource) = Source
    .queue[String](bufferSize = 16, OverflowStrategy.dropHead)
    .toMat(BroadcastHub.sink[String])(Keep.both)
    .run()

  val wordCountSource: Source[String, _] = hubSource

  def broadcastWordCount(wordCount: WordCountMap): Unit = {
    val jsonData = Json.stringify(Json.toJson(wordCount.data))
    hubSink.offer(jsonData)
  }
}
