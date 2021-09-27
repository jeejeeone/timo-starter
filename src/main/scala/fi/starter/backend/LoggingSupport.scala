package fi.starter.backend

import zio.logging.LogAnnotation.optional
import zio.logging.slf4j.Slf4jLogger
import zio.logging.{ LogAnnotation, Logging, log }
import zio.{ ULayer, ZIO }

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

  //TODO: naming
  def trackingAnnotation[Logging, E, A](traceId: TrackingId, spanId: SpanId)(eff: ZIO[Logging, E, A]) =
    log.locally(_.annotate(TraceIdAnnotation, Some(traceId.value)).annotate(SpanIdAnnotation, Some(spanId.value)))(eff)

  def trackingAnnotation[Logging, E, A](traceId: TrackingId)(eff: ZIO[Logging, E, A]) =
    log.locally(_.annotate(TraceIdAnnotation, Some(traceId.value)))(eff)

  def trackingAnnotation[Logging, E, A](spanId: SpanId)(eff: ZIO[Logging, E, A]) =
    log.locally(_.annotate(TraceIdAnnotation, Some(spanId.value)))(eff)

  val logLayer: ULayer[Logging] =
    Slf4jLogger.makeWithAnnotationsAsMdc(List(LogAnnotation.Cause, SpanIdAnnotation, TraceIdAnnotation))
}

case class SpanId(value: String)     extends AnyVal
case class TrackingId(value: String) extends AnyVal
