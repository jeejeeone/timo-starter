package fi.starter.backend

import fi.starter.backend.LoggingFormat.defautJsonFormat
import zio.clock.Clock
import zio.logging.LogAnnotation.optional
import zio.logging.LogAppender.withLoggerNameFromLine
import zio.logging.Logging.{ addTimestamp, modifyLoggerM }
import zio.logging._
import zio.{ ZIO, ZLayer }

object LoggingSupport {
  val SpanIdAnnotation: LogAnnotation[Option[String]] =
    optional[String](
      name = "spanId",
      identity
    )

  val TraceIdAnnotation: LogAnnotation[Option[String]] =
    optional[String](
      name = "traceId",
      identity
    )

  def trackingAnnotation[E, A](traceId: TrackingId)(eff: ZIO[Logging, E, A]) =
    log.locally(_.annotate(TraceIdAnnotation, Some(traceId.value)))(eff)

  val logLayer = ZLayer.requires[Clock] ++
    (LogAppender.console[String](
      LogLevel.Info,
      defautJsonFormat
    ) >>> withLoggerNameFromLine[String]) >+> Logging.make >>> modifyLoggerM(addTimestamp[String])

  /*
    val logLayer: ULayer[Logging] =
    Slf4jLogger.makeWithAnnotationsAsMdc(List(LogAnnotation.Cause, SpanIdAnnotation, TraceIdAnnotation))
   */
}

case class SpanId(value: String)     extends AnyVal
case class TrackingId(value: String) extends AnyVal
