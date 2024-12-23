package services

import models.{BlogPost, WordCountMap}
import scala.collection.mutable
import javax.inject._

@Singleton
class ProcessorService @Inject()() {
  def process(blogPosts: List[BlogPost]): WordCountMap = {
    val wordCounts = mutable.Map[String, Int]()
    
    blogPosts.foreach { post =>
      val words = tokenize(post.content)
      words.foreach { word =>
        val normalizedWord = word.toLowerCase
        wordCounts.put(normalizedWord, wordCounts.getOrElse(normalizedWord, 0) + 1)
      }
    }
    
    WordCountMap(wordCounts.toMap)
  }

  private def tokenize(text: String): List[String] = {
    text.split("\\W+").toList.filter(_.nonEmpty)
  }
}
