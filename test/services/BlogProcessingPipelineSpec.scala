package services

import models.{BlogPost, WordCountMap}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.ArgumentCaptor
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BlogProcessingPipelineSpec extends AnyWordSpec with Matchers with ScalaFutures with MockitoSugar {

  "BlogProcessingPipeline" should {
    "correctly broadcast word counts" in {
      // Mock dependencies
      val fetcherService = mock[FetcherService]
      val processorService = mock[ProcessorService]
      val webSocketBroadcaster = mock[WebSocketBroadcaster]

      val pipeline = new BlogProcessingPipeline(fetcherService, processorService, webSocketBroadcaster)

      // Predefined blog posts
      val blogPosts = List(BlogPost(1, "hello world"), BlogPost(2, "hello scala"))

      // Mock responses
      when(fetcherService.fetchPosts("test-api-url")).thenReturn(Future.successful(blogPosts))
      when(processorService.process(blogPosts)).thenReturn(WordCountMap(Map("hello" -> 2, "world" -> 1, "scala" -> 1)))

      // Execute pipeline
      pipeline.execute("test-api-url").futureValue

      // Capture the broadcasted word count
      val wordCountCaptor = ArgumentCaptor.forClass(classOf[WordCountMap])
      verify(webSocketBroadcaster).broadcastWordCount(wordCountCaptor.capture())

      // Verify accumulated word count
      val capturedWordCount = wordCountCaptor.getValue
      capturedWordCount should be(WordCountMap(Map("hello" -> 2, "world" -> 1, "scala" -> 1)))
    }
  }
}
