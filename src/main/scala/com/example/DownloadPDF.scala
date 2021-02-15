package com.example

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Flow, Framing, Sink, Source}
import akka.util.ByteString

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

object DownloadPDF extends App{

  val systems = ActorSystem[Nothing](Behaviors.empty,"DownloadPDF")

  implicit val classicSystem = systems.classicSystem
  implicit val ec = ExecutionContext.global

  val request = HttpRequest(
    uri = "http://localhost:8080/pdf",
    method = HttpMethods.GET
  )

 val result =  for {
    resp <- Http().singleRequest(request)
    result <- resp match {
      case HttpResponse(StatusCodes.OK, _, entity, _) =>
        entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map { body =>
         // println("Got response, body: " + body.utf8String)
          body.utf8String
        }
      case HttpResponse(err, _, entity, _) => throw new Exception("FAILED")
    }
  }yield result


  val result2 =  for {
    resp <- Http().singleRequest(request)
    result <- resp match {
      case HttpResponse(StatusCodes.OK, _, entity, _) =>
        entity.dataBytes.runReduce(_ ++ _).map(_.utf8String)
      case HttpResponse(err, _, entity, _) =>
        entity.discardBytes()
        throw new Exception("FAILED")
    }
  }yield result

  val processorFlow: Flow[String, Int, NotUsed] =
    Flow[String].map(_.length)

  //val requests = Source.future(Http().singleRequest(request)).via(processorFlow)



 // println(Await.result(result2,Duration.Inf))

  println(Await.result(Source.future(runRequest(request)).via(processorFlow).runWith(Sink.head),Duration.Inf))

  private def runRequest(req: HttpRequest): Future[String] =
    Http()
      .singleRequest(req)
      .flatMap { response =>
        response.entity.dataBytes
          .runReduce(_ ++ _).map(_.utf8String)
      }
}
