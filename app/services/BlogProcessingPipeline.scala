package services

import models.{BlogPost, WordCountMap}
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import play.api.Logging

@Singleton
class BlogProcessingPipeline @Inject() (
    fetcher: FetcherService,
    processor: ProcessorService,
    broadcaster: WebSocketBroadcaster
)(implicit ec: ExecutionContext)
    extends Logging {

  /**
   * Executes the pipeline: fetch -> process -> broadcast.
   * @param apiUrl The WordPress API URL to fetch posts from.
   */
  def execute(apiUrl: String): Future[Unit] = {
    for {
      posts <- fetcher.fetchPosts(apiUrl)
      newWordCount = processor.process(posts)
      _ = broadcastWordCount(newWordCount)
    } yield {
      logger.info("Pipeline executed successfully")
    }
  }

  /**
   * Broadcasts the new word count.
   */
  private def broadcastWordCount(newWordCount: WordCountMap): Unit = {
    broadcaster.broadcastWordCount(newWordCount)
  }
}
