package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class BlogPost(id: Int, content: String)

object BlogPost {
  implicit val blogPostReads: Reads[BlogPost] = (
    (JsPath \ "id").read[Int] and
    (JsPath \ "content" \ "rendered").read[String]
  )(BlogPost.apply _)

  implicit val blogPostWrites: Writes[BlogPost] = (
  (JsPath \ "id").write[Int] and
  (JsPath \ "content" \ "rendered").write[String]
)(unlift(BlogPost.unapply))
  
  implicit val blogPostFormat: Format[BlogPost] = Format(blogPostReads, blogPostWrites)
}
