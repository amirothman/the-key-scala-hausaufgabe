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

  // Maintain accumulated word counts
  private var accumulatedWordCount = WordCountMap(Map.empty[String, Int])

  /**
   * Executes the pipeline: fetch -> process -> broadcast.
   * @param apiUrl The WordPress API URL to fetch posts from.
   */
  def execute(apiUrl: String): Future[Unit] = {
    for {
      posts <- fetcher.fetchPosts(apiUrl)
      newWordCount = processor.process(posts)
      _ = updateAndBroadcastWordCount(newWordCount)
    } yield {
      logger.info("Pipeline executed successfully")
    }
  }

  /**
   * Updates the accumulated word count by merging with new counts and broadcasts the result.
   */
  private def updateAndBroadcastWordCount(newWordCount: WordCountMap): Unit = {
    // Merge new word counts with accumulated counts
    val mergedCounts = newWordCount.data.foldLeft(accumulatedWordCount.data) {
      case (counts, (word, count)) =>
        counts.updated(word, counts.getOrElse(word, 0) + count)
    }
    
    accumulatedWordCount = WordCountMap(mergedCounts)
    broadcaster.broadcastWordCount(accumulatedWordCount)
  }
}
