name    := "Finagle Tinker"

version := "0.1"

scalaVersion := "2.8.1"

artifactPath in (Compile, packageBin) <<=
  baseDirectory(_  / "build" / "application.jar")

mainClass in (Compile, packageBin) := Some("com.robert42.ft.RestServer")

resolvers ++= Seq(
  "scala-tools" at "http://nexus.scala-tools.org/content/repositories/public",
  "java.net"    at "http://download.java.net/maven/2/",
  "memcached"   at "http://files.couchbase.com/maven2/",
  "codahale"    at "http://repo.codahale.com",
  "twttr"       at "http://maven.twttr.com"
)

libraryDependencies ++= Seq(
  "ch.qos.logback"      %  "logback-classic"     % "0.9.29",
  "spy"                 %  "spymemcached"        % "2.6",
  "com.twitter"         %  "finagle-http"        % "1.9.1",
  "com.foursquare"      %% "rogue"               % "1.0.22" intransitive(),
  "net.liftweb"         %% "lift-mongodb-record" % "2.4-M2",
  "com.codahale"        %% "simplespec"          % "0.3.4" % "test->default"
)

seq(sbtassembly.Plugin.assemblySettings: _*)

outputPath in Assembly <<= baseDirectory(_ / "build" / "dependencies.jar")

// test in Assembly := {}
