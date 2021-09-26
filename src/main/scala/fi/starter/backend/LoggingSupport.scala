package fi.starter.backend

import zio.logging.LogAnnotation.optional
import zio.logging.slf4j.Slf4jLogger
import zio.logging.{ LogAnnotation, Logging, log }
import zio.{ Cause, ULayer, ZIO }

/* CAUSE DILEMMA with logstash encoder and zio logging slfj4 bridge */

/*
Motivation:
 log.throwable(msg, throwable) -> message -> msg, error.stack_trace -> stack trace
 log.error(msg, cause) -> message -> msg, STACK TRACE LOST

Cause a zio construct incompatible with throwable, information is lost if logging
with log.throwable

Cause should be annotated with ErrorStackTrace but how to include this in the code base?

Solutions:
  - manual annotation, see errorAnnotation
  - zio.logging.log.cause(msg, cause). How? Probably needs a fork essentially, not great
  - inject logging as service, extend service and implement cause(..)
  - Agree to only use log.throwable (doesn't seem ideal)
 */

object LoggingSupport {
  val ErrorStackTraceAnnotation: LogAnnotation[Option[Cause[Any]]] =
    optional[Cause[Any]](
      name = "errorStackTrace",
      _.prettyPrint
    )

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

  //TODO: Better naming
  def errorAnnotation[Logging, E, A](cause: Cause[Any])(eff: ZIO[Logging, E, A]) =
    log.locally(_.annotate(ErrorStackTraceAnnotation, Some(cause)))(eff)

  def trackingAnnotation[Logging, E, A](traceId: TrackingId, spanId: SpanId)(eff: ZIO[Logging, E, A]) =
    log.locally(_.annotate(TraceIdAnnotation, Some(traceId.value)).annotate(SpanIdAnnotation, Some(spanId.value)))(eff)

  def trackingAnnotation[Logging, E, A](traceId: TrackingId)(eff: ZIO[Logging, E, A]) =
    log.locally(_.annotate(TraceIdAnnotation, Some(traceId.value)))(eff)

  def trackingAnnotation[Logging, E, A](spanId: SpanId)(eff: ZIO[Logging, E, A]) =
    log.locally(_.annotate(TraceIdAnnotation, Some(spanId.value)))(eff)

  val logLayer: ULayer[Logging] =
    Slf4jLogger.makeWithAnnotationsAsMdc(List(ErrorStackTraceAnnotation, SpanIdAnnotation, TraceIdAnnotation))
}

case class SpanId(value: String)     extends AnyVal
case class TrackingId(value: String) extends AnyVal
