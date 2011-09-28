package com.robert42.ft

import org.jboss.netty.handler.codec.http.DefaultHttpResponse
import org.jboss.netty.handler.codec.http.
  {HttpHeaders => Headers, HttpResponseStatus => Status}
import org.jboss.netty.handler.codec.http.HttpHeaders.Names._
import org.jboss.netty.handler.codec.http.HttpHeaders.Values._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.util.CharsetUtil.UTF_8
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

object Responses {
  def status(status: Status) = new DefaultHttpResponse(HTTP_1_1, status)

  def json(data: String, gzip: Boolean = false) = {
    val response    = status(OK)
    val contentType = "application/json; charset=%s"
      .format(UTF_8.toString.toLowerCase)
    response.setHeader(CONTENT_TYPE, contentType)
    if (gzip) response.setHeader(CONTENT_ENCODING, GZIP)
    response setContent content(data, gzip)
    Headers.setContentLength(response, response.getContent.readableBytes)
    response
  }

  def error(info: String, gzip: Boolean = false) = {
    val response    = status(INTERNAL_SERVER_ERROR)
    val contentType = "text/plain; charset=%s"
      .format(UTF_8.toString.toLowerCase)
    response.setHeader(CONTENT_TYPE, contentType)
    if (gzip) response.setHeader(CONTENT_ENCODING, GZIP)
    response setContent content(info, gzip)
    Headers.setContentLength(response, response.getContent.readableBytes)
    response
  }

  def content(data: String, gzip: Boolean = false) = {
    if (gzip) {
      val bytes   = new ByteArrayOutputStream
      val gzipper = new GZIPOutputStream(bytes)
      gzipper write data.getBytes(UTF_8)
      gzipper.finish
      copiedBuffer(bytes.toByteArray)
    } else copiedBuffer(data, UTF_8)
  }
}
