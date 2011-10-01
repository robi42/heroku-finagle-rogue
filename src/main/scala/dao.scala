package com.robert42.ft

import TodoXmlData._
import com.twitter.conversions.time._
import net.liftweb.record.Record
import net.liftweb.json._
import net.liftweb.json.Serialization.read
import com.foursquare.rogue.Rogue._
import org.bson.types.ObjectId
import xml._
import java.util.Calendar

// Persistence layer interface.
object Todos extends Storable {
  private lazy val cache = Memcached.cache
  private lazy val todos = "todos"

  def fromJson(json: String, create: Boolean, update: Boolean) = {
    if (create)
      createFromJson(read[TodoCreateJson](json))
    else if (update)
      updateFromJson(read[TodoUpdateJson](json))
    else throw makeFlagsError
  }

  private def createFromJson(data: TodoCreateJson) = {
    val record = Todo.createRecord
      .text(data.text)
      .order(data.order)
      .done(data.done)
      .save
    debug("record: %s" format record)
    record
  }

  private def updateFromJson(data: TodoUpdateJson) = {
    val modify = Todo where (_.id eqs new ObjectId(data._id)) findAndModify
      (_.text setTo data.text) and
      (_.done setTo data.done) and
      (_.modifiedAt setTo Calendar.getInstance)
    val record = modify.updateOne(returnNew = true).get
    debug("record: %s" format record)
    record
  }

  def fromXml(xml: String, create: Boolean, update: Boolean) = {
    val data = XML loadString xml
    if (create) createFromXml(data)
    else if (update) updateFromXml(data)
    else throw makeFlagsError
  }

  private def createFromXml(data: NodeSeq) = {
    val record = Todo.createRecord
      .text((data \ TextElem.toString).text)
      .order((data \ OrderElem.toString).text.toInt)
      .done((data \ DoneElem.toString).text.toBoolean)
      .save
    debug("record: %s" format record)
    record
  }

  private def updateFromXml(data: NodeSeq) = {
    val id     = (data \ IdAttr.toString).text
    val modify = Todo where (_.id eqs new ObjectId(id)) findAndModify
      (_.text setTo (data \ TextElem.toString).text) and
      (_.done setTo (data \ DoneElem.toString).text.toBoolean) and
      (_.modifiedAt setTo Calendar.getInstance)
    val record = modify.updateOne(returnNew = true).get
    debug("record: %s" format record)
    record
  }

  def get(id: String) = {
    val query  = Todo where (_.id eqs new ObjectId(id)) get
    val record = query.get
    debug("record: %s" format record)
    record
  }

  def all = {
    val records = Todo.findAll
    debug("records: %s" format records)
    records
  }

  def allAsJson = {
    val cached = cache get todos
    val json   = if (cached != null) cached.toString else {
      val data = compact(render(JArray(Todo.findAll.map(_.asJValue))))
      cache.set(todos, 7.days.inMillis.toInt, data)
      data
    }
    debug("JSON: %s" format json)
    json
  }

  def remove(id: String) = {
    val query  = Todo where (_.id eqs new ObjectId(id)) get
    val record = query.get
    debug("record %s" format record)
    record.delete_!
  }
}
