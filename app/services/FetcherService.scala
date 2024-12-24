package services

import models.BlogPost
import play.api.libs.json.Json
import sttp.client3._
import sttp.client3.playJson._
import sttp.model.Uri

import scala.concurrent.{ExecutionContext, Future}
import javax.inject._
import scala.util.Try
import play.api.Logger

@Singleton
class FetcherService @Inject()(implicit ec: ExecutionContext) {
  private val logger = Logger(this.getClass)
  private val processedPostIds = scala.collection.mutable.Set[Int]()

  private val defaultBackend = HttpURLConnectionBackend()

  def fetchPosts(apiUrl: String, customBackend: Option[SttpBackend[Identity, Any]] = None): Future[List[BlogPost]] = {
    logger.info(s"Fetching posts from $apiUrl")
    val backend = customBackend.getOrElse(defaultBackend)
    val uri = Uri.parse(apiUrl).getOrElse {
      logger.error(s"Invalid URL provided: $apiUrl")
      throw new IllegalArgumentException(s"Invalid URL: $apiUrl")
    }
    val request = basicRequest.get(uri)
    val response = request.send(backend)

    response.body match {
      case Right(json) =>
        val posts = Json.parse(json).as[List[BlogPost]]
        val newPosts = posts.filterNot(post => processedPostIds.contains(post.id))
        processedPostIds ++= newPosts.map(_.id)
        logger.info(s"Found ${newPosts.length} new posts")
        Future.successful(newPosts)
      case Left(error) =>
        logger.error(s"Failed to fetch posts: $error")
        Future.failed(new RuntimeException(s"Failed to fetch posts: $error"))
    }
  }
}
