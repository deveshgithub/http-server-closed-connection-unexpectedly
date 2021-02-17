package com.example

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.caching.LfuCache
import akka.http.caching.scaladsl.{Cache, CachingSettings}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.server.directives.CachingDirectives.cache
import akka.http.scaladsl.server.{RequestContext, Route, RouteResult}

import scala.concurrent.duration.DurationInt
import scala.util.Failure
import scala.util.Success

//#main-class
object StartHttpServer extends App {
  //#start-http-server
  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    // Akka HTTP still needs a classic ActorSystem to start
    import system.executionContext



    val futureBinding = Http().newServerAt("localhost", 8080).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  //#start-http-server


  //#server-bootstrapping
  val rootBehavior = Behaviors.setup[Nothing] { context =>

    val routes = new DocumentRoute()(context.system)

    //12670131

    val keyerFunction: PartialFunction[RequestContext, Uri] = {
      case r: RequestContext => r.request.uri.withRawQueryString("")
    }
    val defaultCachingSettings = CachingSettings(context.system)

    val lfuCacheSettings =
      defaultCachingSettings.lfuCacheSettings
        .withInitialCapacity(1024)
        .withMaxCapacity(2048)
        .withTimeToLive(90.minutes)
        .withTimeToIdle(90.minutes)

    val cachingSettings =
      defaultCachingSettings.withLfuCacheSettings(lfuCacheSettings)

    val lfuCache: Cache[Uri, RouteResult] = LfuCache(cachingSettings)

    val route = cache(lfuCache, keyerFunction)(routes.pdfRoutes)

    startHttpServer(route)(context.system)

    Behaviors.empty
  }

  val system = ActorSystem[Nothing](rootBehavior, "PDFDownloadActorSystem")




  //#server-bootstrapping

}

//#main-class
