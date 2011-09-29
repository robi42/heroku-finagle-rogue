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
      try {
        request.method -> Path(request.path) match {
          case GET -> Root / "todos" => Future.value {
            val data = Todos.allAsJson
            debug("data: %s" format data)
            Responses.json(data, acceptsGzip(request))
          }
          case GET -> Root / "todos" / id => Future.value {
            val todo = Todos get id
            val data = todo.toJson
            debug("data: %s" format data)
            Responses.json(data, acceptsGzip(request))
          }
          case POST -> Root / "todos" => Future.value {
            val content = request.getContent.toString(UTF_8)
            val todo    = Todos.fromJson(content, create = true)
            val data    = todo.toJson
            Responses.json(data, acceptsGzip(request))
          }
          case PUT -> Root / "todos" / id => Future.value {
            val content = request.getContent.toString(UTF_8)
            val todo    = Todos.fromJson(content, update = true)
            val data    = todo.toJson
            debug("data: %s" format data)
            Responses.json(data, acceptsGzip(request))
          }
          case DELETE -> Root / "todos" / id => Future.value {
            Todos remove id
            debug("data: %s" format id)
            Responses.status(OK)
          }
          case _ =>
            Future value Responses.status(NOT_FOUND)
        }
      } catch {
        case e: NoSuchElement => Future value Responses.status(NOT_FOUND)
        case e: Exception => Future.value {
          val message = Option(e.getMessage) getOrElse "Something went wrong."
          error("message: %s - stack trace: %s"
            .format(message, e.getStackTraceString))
          Responses.error(message, acceptsGzip(request))
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
