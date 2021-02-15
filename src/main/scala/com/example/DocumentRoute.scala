package com.example

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.caching.LfuCache
import akka.http.caching.scaladsl.{Cache, CachingSettings}
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.CachingDirectives.cache
import akka.http.scaladsl.server.{RequestContext, Route, RouteResult}
import akka.util.Timeout
import com.example.Constant.fileContent


import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

//#import-json-formats
//#user-routes-class
class DocumentRoute()(implicit val system: ActorSystem[_]) {

  //#user-routes-class

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  //#import-json-formats

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))
  implicit val ec = ExecutionContext.global
  var index = 0


  val getPDF: Future[String] = {

    Future(fileContent)
  }

  //#all-routes
  //#users-get-post
  //#users-get-delete
  val pdfRoutes: Route =
  pathPrefix("pdf") {
    concat(
      //#users-get-delete
      pathEnd {
        concat(
          get {
            index = index + 1
            println("Sending pdf " + index)
            complete(getPDF)
          },
          post {
            entity(as[String]) { pdf =>
              onSuccess(getPDF) { performed => {
                index = index + 1
                println("Sending pdf " + index)
                complete((StatusCodes.Created, performed))
              }
              }
            }
          })
      })
  }
  //#users-get-delete

  //#all-routes
}
