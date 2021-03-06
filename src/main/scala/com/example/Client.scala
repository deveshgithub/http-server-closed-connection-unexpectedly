package com.example

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object Client extends App {

  implicit val system: ActorSystem = ActorSystem("HTTPCLIENT")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher


  val result1 = Await.result(Http().singleRequest(HttpRequest(uri = "http://localhost:8080/pdf")), Duration.Inf)
  println(s" SHOULD BE CACHED NOW ${result1.status}")

  val result2 = Await.result(Http().singleRequest(HttpRequest(uri = "http://localhost:8080/pdf")), Duration.Inf)
  println(s" FETCH CACHED RESPONSE  ${result2.status}")

  //https://doc.akka.io/docs/akka-http/current/client-side/connection-level.html#opening-http-connections
  def connectionFlow: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] =
    Http().connectionTo("localhost").toPort(8080).http()

  def dispatchRequest(request: HttpRequest): Future[HttpResponse] =
    Source.single(request)
      .via(connectionFlow)
      .runWith(Sink.head)

  val resp: Unit = (0 to 1500).foreach(index => {
    val responseFuture = dispatchRequest(HttpRequest(uri = "/pdf"))

    responseFuture.map {
      case response@HttpResponse(StatusCodes.OK, headers, entity, _) =>
        // println(s"Response => ${Unmarshal(entity).to[String]}")

        println(Console.GREEN + index + "LENGTH=> " + entity.getContentLengthOption())
        Unmarshal(entity).to[String].map(r => {

        }
        )
      case _ =>
        println(Console.RED + " GOT ERROR ")
    }

    responseFuture onComplete {
      case Success(value) =>
        println(Console.GREEN + s" success ${value.status}")
      case Failure(exception) =>
        println(Console.RED + "index = " + index + "   " + exception.toString)
      //exception.printStackTrace()
    }
  })


}
