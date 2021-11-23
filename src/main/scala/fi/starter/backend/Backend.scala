package fi.starter.backend

import fi.starter.backend.logging.Annotations.withTracking
import fi.starter.backend.logging.LoggingSupport.logLayer
import fi.starter.backend.logging.{SpanId, loggedEffect, tracking}
import org.slf4j.LoggerFactory
import zhttp.http._
import zhttp.service.Server
import zio._
import zio.logging.{Logging, log}
import fi.starter.backend.aspect.EffectAspectSyntax

//TODO: logging middleware

object Backend extends App {
  val slf4jLog = LoggerFactory.getLogger("slf4j")

  val app: HttpApp[Logging, Nothing] = HttpApp.collectM {
    case Method.GET -> Root / "text" =>
      // Annotations stored as FiberRef (kind of like java threadlocal)
      withTracking(SpanId("123")) {
        log
          .info("zio hello")
          .zipRight(loggedEffect {
            slf4jLog.info("slf4j hello")
          }.catchAll(_ => ZIO.unit))
          .zipRight(ZIO.effectTotal(Response.text("Hello World!")))
      }
    case Method.GET -> Root / "cause" =>
      (log.info("ok") *>
        ZIO
          .fail(Cause.fail("fail"))
          .catchAllCause(cause =>
            log.error("msg", cause) *> ZIO.effectTotal(Response.text("Logging some causes"))
          )) @@ tracking(SpanId("666"))
  }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    Server
      .start(8090, app.silent)
      .provideSomeLayer(logLayer)
      .exitCode
}
