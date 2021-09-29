package fi.starter.backend.logging

import zio.ZIO
import zio.logging.LogAnnotation.optional
import zio.logging.{LogAnnotation, Logging, log}

object Annotations {
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
}

case class SpanId(value: String)     extends AnyVal
case class TrackingId(value: String) extends AnyVal
