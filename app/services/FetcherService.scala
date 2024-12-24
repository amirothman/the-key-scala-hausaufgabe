package services

import models.BlogPost
import play.api.libs.json.Json
import sttp.client3._
import sttp.client3.playJson._
import sttp.model.Uri

import scala.concurrent.{ExecutionContext, Future}
import javax.inject._
import scala.util.Try

@Singleton
class FetcherService @Inject()(implicit ec: ExecutionContext) {
  private val processedPostIds = scala.collection.mutable.Set[Int]()

  private val defaultBackend = HttpURLConnectionBackend()

  def fetchPosts(apiUrl: String, customBackend: Option[SttpBackend[Identity, Any]] = None): Future[List[BlogPost]] = {
    val backend = customBackend.getOrElse(defaultBackend)
    val uri = Uri.parse(apiUrl).getOrElse(throw new IllegalArgumentException(s"Invalid URL: $apiUrl"))
    val request = basicRequest.get(uri)
    val response = request.send(backend)

    response.body match {
      case Right(json) =>
        val posts = Json.parse(json).as[List[BlogPost]]
        val newPosts = posts.filterNot(post => processedPostIds.contains(post.id))
        processedPostIds ++= newPosts.map(_.id)
        Future.successful(newPosts)
      case Left(error) =>
        Future.failed(new RuntimeException(s"Failed to fetch posts: $error"))
    }
  }
}
