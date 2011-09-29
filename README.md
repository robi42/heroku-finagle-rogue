# Heroku Finagle Rogue App

This is a minimal [Scala] app targeting deployment to [Heroku]. <br>
Its web layer is based on [Finagle] and its persistence layer on [Rogue]. <br>
Basically, this app's a RESTful service API.

## Dev

The app's persistence layer depends on [MongoDB], so start it:

    $ mongod

To run the app locally via `foreman`:

    $ gem install foreman
    $ foreman start

Then point your browser to this URL:

  http://localhost:5000/

Note: you need to build before (see below).

To compile + package Scala on-the-fly <br>
via `sbt` [0.10]:

    $ sbt
    > ~package

For compiling/packaging once just leave off the `~`.

To make an assembly of all `sbt` project lib dependencies:

    $ sbt
    > assembly:package-dependency

## Deploy

    $ gem install heroku
    $ heroku create --stack cedar
    $ heroku addons:add mongolab
    $ heroku config

Now, adjust `src/main/resources/props/production.default.props` according to `MONGOLAB_URI`. <br>
Plus, rebuild `application.jar` (via `sbt package`) to include this config update.

    $ heroku config:add LIFT_PROD=-Drun.mode=production
    $ git commit -am 'Make it ready for production.'
    $ git push heroku master

Then, make the project's lib dependencies JAR (via `sbt assembly:package-dependency`).

    $ git commit -am 'Add lib dependencies.'
    $ git push heroku master
    $ heroku open


  [Scala]:   http://www.scala-lang.org/
  [Heroku]:  http://www.heroku.com/
  [Finagle]: http://twitter.github.com/finagle/
  [Rogue]:   http://engineering.foursquare.com/2011/01/21/rogue-a-type-safe-scala-dsl-for-querying-mongodb/
  [MongoDB]: http://www.mongodb.org/
  [0.10]:    https://github.com/harrah/xsbt/wiki/Setup
