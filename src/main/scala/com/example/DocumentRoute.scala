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

import java.time.LocalDateTime

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future, blocking}

//#import-json-formats
//#user-routes-class
class DocumentRoute()(implicit val system: ActorSystem[_]) {

  //#user-routes-class

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  //#import-json-formats

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))
  implicit val ec = ExecutionContext.global


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
            parameters('index.?) { index: Option[String] =>
              onSuccess(slowOp) {
                println(s"[${LocalDateTime.now}] --> Sending pdf ${index.getOrElse("")}")
                complete(getPDF)
              }
            }
          },
          post {
            entity(as[String]) { pdf =>
              onSuccess(getPDF) { performed => {
                println("Creating pdf " + performed)
                complete((StatusCodes.Created, performed))
              }
              }
            }
          })
      })
  }
  //#users-get-delete

  def slowOp: Future[Unit] = Future {
    blocking {
      Thread.sleep(5000)
    }
  }
  //#all-routes
}
