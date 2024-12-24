package services

import models.BlogPost
import org.scalatestplus.play.PlaySpec
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json
import sttp.client3.testing.SttpBackendStub
import sttp.client3.Response
import scala.concurrent.ExecutionContext.Implicits.global

class FetcherServiceSpec extends PlaySpec with ScalaFutures with Matchers {

  "FetcherService" should {
    "fetch and filter new posts correctly" in {
      val service = new FetcherService()
      val mockAPIResponse: String = """
      [
        {
          "id": 1,
          "content": {
            "rendered": "First post"
          }
        },
        {
          "id": 2,
          "content": {
            "rendered": "Second post"
          }
        }
      ]
      """

      val apiUrl = "http://mock.api"
      val mockBackend = SttpBackendStub.synchronous
        .whenRequestMatchesPartial {
          case req if req.uri.toString == apiUrl =>
            Response.ok(mockAPIResponse)
        }

      val posts = service.fetchPosts(apiUrl, Some(mockBackend)).futureValue
      posts must contain theSameElementsAs(List(
        BlogPost(1, "First post"),
        BlogPost(2, "Second post")
      ))

      // Test that duplicate posts are filtered out
      val secondFetch = service.fetchPosts(apiUrl, Some(mockBackend)).futureValue
      secondFetch must be(empty)
    }
  }
}
