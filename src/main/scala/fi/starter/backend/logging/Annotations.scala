package fi.starter.backend.logging

import zio.ZIO
import zio.logging.LogAnnotation.optional
import zio.logging.{ LogAnnotation, Logging, log }

object Annotations {
  val SpanIdAnnotation: LogAnnotation[Option[SpanId]] =
    optional[SpanId](
      name = "spanId",
      _.value
    )

  val TraceIdAnnotation: LogAnnotation[Option[TraceId]] =
    optional[TraceId](
      name = "traceId",
      _.value
    )

  def withTracking[E, A](spanId: SpanId)(eff: ZIO[Logging, E, A]) =
    log.locally(_.annotate(SpanIdAnnotation, Some(spanId)))(eff)
}

case class SpanId(value: String)
case class TraceId(value: String)
