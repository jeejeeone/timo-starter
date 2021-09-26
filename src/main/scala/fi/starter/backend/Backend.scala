package fi.starter.backend

import fi.starter.backend.LoggingSupport.{ errorAnnotation, logLayer, trackingAnnotation }
import zhttp.http._
import zhttp.service.Server
import zio._
import zio.logging.{ Logging, log }

object Backend extends App {
  val app: HttpApp[Logging, Nothing] = HttpApp.collectM {
    case Method.GET -> Root / "text"  =>
      // Annotations stored as FiberRef (kind of like java threadlocal)
      trackingAnnotation(TrackingId("123")) {
        log.info("Some logging with traceId and spanId") *> ZIO.effectTotal(Response.text("Hello World!"))
      }
    case Method.GET -> Root / "cause" =>
      trackingAnnotation(TrackingId("123"), SpanId("123123")) {
        ZIO
          .fail(Cause.fail("fail"))
          .catchAllCause(cause =>
            errorAnnotation(cause) {
              log.error("msg", cause)
            } *> ZIO.effectTotal(Response.text("Logging some causes"))
          )
      }
  }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    Server
      .start(8090, app.silent)
      .provideSomeLayer(logLayer)
      .exitCode
}
