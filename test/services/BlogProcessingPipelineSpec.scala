package services

import models.{BlogPost, WordCountMap}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.mockito.ArgumentMatchers._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
class BlogProcessingPipelineSpec extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures {
class BlogProcessingPipelineSpec extends PlaySpec with GuiceOneAppPerSuite {

  "BlogProcessingPipeline" should {

    "execute the pipeline successfully" in {
      // Mock services
      val mockFetcher = mock[FetcherService](classOf[FetcherService])
      val mockProcessor = mock[ProcessorService](classOf[ProcessorService])
      val mockBroadcaster = mock[WebSocketBroadcaster](classOf[WebSocketBroadcaster])

      // Mock behavior
      val mockPosts = List(BlogPost(1, "This is a test post."))
      val mockWordCount = WordCountMap(Map("this" -> 1, "is" -> 1, "a" -> 1, "test" -> 1, "post" -> 1))

      when(mockFetcher.fetchPosts(anyString())).thenReturn(Future.successful(mockPosts))
      when(mockProcessor.process(mockPosts)).thenReturn(mockWordCount)

      // Create pipeline
      val pipeline = new BlogProcessingPipeline(mockFetcher, mockProcessor, mockBroadcaster)

      // Execute pipeline
      val result = pipeline.execute("http://example.com/api/posts").futureValue

      // Verify behavior
      verify(mockFetcher).fetchPosts("http://example.com/api/posts")
      verify(mockProcessor).process(mockPosts)
      verify(mockBroadcaster).broadcastWordCount(mockWordCount)

      // Check result
      result mustBe ()
    }
  }
  }
}
