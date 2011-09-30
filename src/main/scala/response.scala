package com.robert42.ft

import org.jboss.netty.handler.codec.http.HttpHeaders.Names._
import org.jboss.netty.handler.codec.http.HttpHeaders.Values._
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.util.CharsetUtil.UTF_8
import com.twitter.finagle.http.Response
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.Version.Http11
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

object Responses {
  def json(data: String, gzip: Boolean = false) = {
    val response = Response()
    response.setContentTypeJson
    if (gzip) response.setHeader(CONTENT_ENCODING, GZIP)
    response.content = content(data, gzip)
    response
  }

  def error(info: String, gzip: Boolean = false) = {
    val response = Response(Http11, InternalServerError)
    response.mediaType = "text/plain"
    if (gzip) response.setHeader(CONTENT_ENCODING, GZIP)
    response.content = content(info, gzip)
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
