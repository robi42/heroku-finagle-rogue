package com.robert42.ft

import org.jboss.netty.handler.codec.http.DefaultHttpResponse
import org.jboss.netty.handler.codec.http.{HttpResponseStatus => Status}
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.util.CharsetUtil.UTF_8

object Responses {
  def status(status: Status) = new DefaultHttpResponse(HTTP_1_1, status)

  def json(data: String) = {
    val response = status(OK)
    response.setHeader("Content-Type", "application/json; charset=utf-8")
    response setContent content(data)
    response
  }

  def error(info: String) = {
    val response = status(INTERNAL_SERVER_ERROR)
    response.setHeader("Content-Type", "text/plain; charset=urf-8")
    response setContent content(info)
    response
  }

  def content(data: String) = copiedBuffer(data, UTF_8)
}
