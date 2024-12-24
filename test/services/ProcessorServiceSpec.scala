package services

import models.{BlogPost, WordCountMap}
import org.scalatestplus.play.PlaySpec
import org.scalatest.matchers.must.Matchers

class ProcessorServiceSpec extends PlaySpec with Matchers {
  "ProcessorService" should {
    "generate a correct word count map" in {
      val service = new ProcessorService()
      val blogPosts = List(
        BlogPost(1, "Hello world! Hello again."),
        BlogPost(2, "Scala is great.")
      )

      val result = service.process(blogPosts)

      result.data must contain theSameElementsAs Map(
        "hello" -> 2,
        "world" -> 1,
        "again" -> 1,
        "scala" -> 1,
        "is" -> 1,
        "great" -> 1
      )
    }

    "handle empty content" in {
      val service = new ProcessorService()
      val blogPosts = List(
        BlogPost(1, ""),
        BlogPost(2, "   ")
      )

      val result = service.process(blogPosts)
      result.data must be(empty)
    }

    "handle special characters and multiple spaces" in {
      val service = new ProcessorService()
      val blogPosts = List(
        BlogPost(1, "Hello,    world! @#$%"),
        BlogPost(2, "Hello... World!!!")
      )

      val result = service.process(blogPosts)
      result.data must contain theSameElementsAs Map(
        "hello" -> 2,
        "world" -> 2
      )
    }
  }
}
