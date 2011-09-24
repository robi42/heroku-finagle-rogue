package com.robert42.ft

import net.liftweb.common.Logger
import net.liftweb.record.Record
import net.liftweb.record.field._
import net.liftweb.json._
import java.lang.IllegalArgumentException

// API contract -- a.k.a. DAO.
trait Storable extends Logger {
  protected[this] implicit val formats = Serialization.formats(NoTypeHints)

  protected[this] val flagsErrorMessage = "Either `create` or `update` must be `true`."

  protected[this] def makeFlagsError = new IllegalArgumentException(flagsErrorMessage)

  def fromJson(json: String, create: Boolean = false, update: Boolean = false): Record[_]
  def fromXml(xml: String, create: Boolean = false, update: Boolean = false): Record[_]
  def get(id: String): Record[_]
  def all: List[Record[_]]
  def allAsJson: String
  def remove(id: String): Unit
}

// Timestamps mixin.
trait Timestampable[OwnerType <: Record[OwnerType]] {
  object createdAt  extends DateTimeField(this.asInstanceOf[OwnerType]) {
    override def asJValue = JInt(value.getTimeInMillis)
  }
  object modifiedAt extends OptionalDateTimeField(this.asInstanceOf[OwnerType]) {
    override def asJValue = value match {
      case Some(value) => JInt(value.getTimeInMillis)
      case None        => JNull
    }
  }
}

// JSON serialization mixin.
trait JsonSerializable[OwnerType <: Record[OwnerType]] {
  def toJson = compact(render(this.asInstanceOf[OwnerType].asJValue))
}

// JSON data contracts.
abstract class JsonData
