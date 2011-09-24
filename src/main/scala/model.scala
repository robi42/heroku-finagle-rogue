package com.robert42.ft

import net.liftweb.mongodb.record._
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import net.liftweb.json.JsonDSL._

// Model definitions.
class Todo private() extends MongoRecord[Todo] with ObjectIdPk[Todo]
                                               with Timestampable[Todo]
                                               with JsonSerializable[Todo] {
  def meta = Todo

  object text  extends StringField(this, 12)
  object order extends LongField(this)
  object done  extends BooleanField(this)
}

object Todo extends Todo with MongoMetaRecord[Todo] {
  ensureIndex(("order" -> 1), true)
}

// For JSON serialization.
final case class TodoCreateJson(text: String, order: Long, done: Boolean)
  extends JsonData
final case class TodoUpdateJson(_id: String, text: String, done: Boolean)
  extends JsonData

// For XML serialization.
object TodoXmlData extends Enumeration("@id", "text", "order", "done") {
  type TodoXmlData = Value
  val  IdAttr, TextElem, OrderElem, DoneElem = Value
}
