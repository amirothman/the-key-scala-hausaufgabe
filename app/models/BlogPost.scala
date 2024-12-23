package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class BlogPost(id: String, content: String)

object BlogPost {
  implicit val blogPostReads: Reads[BlogPost] = (
    (JsPath \ "id").read[String] and
    (JsPath \ "content" \ "rendered").read[String]
  )(BlogPost.apply _)

  implicit val blogPostWrites: Writes[BlogPost] = Json.writes[BlogPost]
  
  implicit val blogPostFormat: Format[BlogPost] = Format(blogPostReads, blogPostWrites)
}
