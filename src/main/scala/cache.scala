package com.robert42.ft

import net.liftweb.util.Props
import net.spy.memcached.MemcachedClient
import java.net.InetSocketAddress

object Memcached {
  private lazy val host = Props.get("memcached.host", "localhost")
  private lazy val port = Props.getInt("memcached.port", 11211)

  lazy val cache = new MemcachedClient(new InetSocketAddress(host, port))
}
