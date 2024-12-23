package controllers

import play.api.mvc._
import org.apache.pekko.stream.scaladsl.{Flow, Sink, Source}
import org.apache.pekko.stream.Materializer
import services.WebSocketBroadcaster
import play.api.http.websocket._

import javax.inject._

@Singleton
class WebSocketController @Inject() (
    broadcaster: WebSocketBroadcaster,
    cc: ControllerComponents
)(implicit mat: Materializer)
    extends AbstractController(cc) {

  def wordCountSocket: WebSocket = WebSocket.accept[Message, Message] { _ =>
    val outFlow = broadcaster.wordCountSource
      .map(TextMessage.apply)
      .mapMaterializedValue(_ => ())

    Flow.fromSinkAndSource(
      sink = Sink.ignore,
      source = outFlow
    )
  }
}
