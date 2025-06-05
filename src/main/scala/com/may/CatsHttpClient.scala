package com.may

import com.may.CatsHttpClient.{Cat, CatsResponse}
import com.github.pjfanning.pekkohttpcirce.FailFastCirceSupport.unmarshaller
import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model.headers.RawHeader
import org.apache.pekko.http.scaladsl.model.{HttpRequest, StatusCodes, Uri}
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshal
import org.apache.pekko.stream.scaladsl.{Sink, Source}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}

trait CatsHttpClient {
  def getAllBreads: Future[Seq[Cat]]
}

object CatsHttpClient {

  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

  @ConfiguredJsonCodec case class CatsResponse(data: Seq[Cat], nextPageUrl: Option[String]) // requires -Ymacro-annotations in build.sbt

  @ConfiguredJsonCodec case class Cat(breed: String, country: String, origin: String, coat: String, pattern: String) // requires -Ymacro-annotations in build.sbt

  def apply(implicit system: ActorSystem[_], ec: ExecutionContext): CatsHttpClient = new CatsHttpClientImpl()
}

class CatsHttpClientException extends Exception

class CatsHttpClientImpl(implicit system: ActorSystem[_], ec: ExecutionContext) extends CatsHttpClient {
  val logger: Logger = LoggerFactory.getLogger(classOf[CatsHttpClientImpl])
  val start: Option[String] = Some("https://catfact.ninja/breeds")

  override def getAllBreads: Future[Seq[Cat]] = {
    Source
      .unfoldAsync(start) {
        case Some(next) =>
          val nextChunkFuture: Future[CatsResponse] = sendRequest(next)

          nextChunkFuture.map { resp =>
            resp.nextPageUrl match {
              case Some(url) => Some((Some(url), resp.data))
              case None => Some((None, resp.data))
            }
          }
        case None => Future.successful(None)
      }
      .runWith(Sink.fold(Seq(): Seq[Cat])(_ ++ _))
  }

  private def sendRequest(url: String): Future[CatsResponse] = {
    logger.info(s"CatsHttpClientImpl: Sending request $url")

    val request = HttpRequest(
      uri = Uri(url),
      headers = List(
        RawHeader("Accept", "application/json")
      )
    )
    Http(system).singleRequest(request).flatMap { response =>
      response.status match {
        case StatusCodes.OK =>
          logger.info("CatsHttpClientImpl: Received success")
          Unmarshal(response.entity).to[CatsResponse]

        case _ =>
          logger.error("CatsHttpClientImpl: Received error")
          throw new CatsHttpClientException()
      }
    }
  }
}
