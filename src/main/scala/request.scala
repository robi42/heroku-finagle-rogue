package com.robert42.ft

import org.jboss.netty.handler.codec.http.HttpHeaders.Names._
import org.jboss.netty.handler.codec.http.HttpHeaders.Values._
import com.twitter.finagle.http.Request

object Requests {
  def acceptsGzip(request: Request) =
    if (request.containsHeader(ACCEPT_ENCODING))
      request.getHeader(ACCEPT_ENCODING).split(",").contains(GZIP)
    else false
}
