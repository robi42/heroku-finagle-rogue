package com.robert42.ft

import Requests._
import java.net.InetSocketAddress
import java.util.{NoSuchElementException => NoSuchElement}
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.util.CharsetUtil.UTF_8
import com.twitter.util.Future
import com.twitter.finagle.http.{Http, Request}
import com.twitter.finagle.http.path._
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finagle.builder.{Server, ServerBuilder}
import net.liftweb.common.Logger

/**
 * A rather sophisticated REST server that handles exceptions.
 */
object RestServer extends Logger {
  /**
   * A simple Filter that catches exceptions and converts them to appropriate
   * HTTP responses.
   */
  class HandleExceptions extends SimpleFilter[HttpRequest, HttpResponse] {
    def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]) = {
      // `handle` asynchronously handles exceptions.
      service(request) handle { case error =>
        val statusCode = error match {
          case _: IllegalArgumentException =>
            FORBIDDEN
          case _ =>
            INTERNAL_SERVER_ERROR
        }
        Responses error error.getMessage
      }
    }
  }

  /**
   * The service itself.
   */
  class Respond extends Service[HttpRequest, HttpResponse] with Logger {
    def apply(httpRequest: HttpRequest) = {
      val request = Request(httpRequest)
      val gzip    = acceptsGzip(request)
      try {
        request.method -> Path(request.path) match {
          case GET -> Root / "todos" => {
            val data = Todos.allAsJson
            debug("Data: %s" format data)
            Future value Responses.json(data, gzip)
          }
          case GET -> Root / "todos" / id => {
            val todo = Todos get id
            val data = todo.toJson
            debug("Data: %s" format data)
            Future value Responses.json(data, gzip)
          }
          case POST -> Root / "todos" => {
            val content = request.getContent.toString(UTF_8)
            val todo    = Todos.fromJson(content, create = true)
            val data    = todo.toJson
            Future value Responses.json(data, gzip)
          }
          case PUT -> Root / "todos" / id => {
            val content = request.getContent.toString(UTF_8)
            val todo    = Todos.fromJson(content, update = true)
            val data    = todo.toJson
            debug("Data: %s" format data)
            Future value Responses.json(data, gzip)
          }
          case DELETE -> Root / "todos" / id => {
            Todos remove id
            debug("Data: %s" format id)
            Future value Responses.status(OK)
          }
          case _ =>
            Future value Responses.status(NOT_FOUND)
        }
      } catch {
        case e: NoSuchElement => Future value Responses.status(NOT_FOUND)
        case e: Exception => {
          Future value Responses.error(e.getMessage, gzip)
          throw e
        }
      }
    }
  }

  def main(args: Array[String]) {
    MongoConfig.init

    val handleExceptions = new HandleExceptions
    val respond          = new Respond

    // Compose the Filters and Service together:
    val service = handleExceptions andThen respond
    val envPort = System getenv "PORT"
    val port    = if (envPort != null) envPort.toInt else 8888
    val server  = ServerBuilder()
      .codec(Http())
      .bindTo(new InetSocketAddress(port))
      .name("restserver")
      .build(service)

    info("Server started on port: %s" format port)
  }
}
