package fi.starter.backend.logging

import zio.Cause
import zio.json.EncoderOps
import zio.logging.{ LogAnnotation, LogContext, LogFormat }

import scala.collection.immutable.ListMap

object LoggingFormat {
  private val NL = System.lineSeparator()

  final case class EcsJsonFormat(customFields: Map[String, String] = Map()) extends LogFormat[String] {
    override def format(context: LogContext, line: String): String = {
      val date       = context(LogAnnotation.Timestamp)
      val level      = context(LogAnnotation.Level)
      val loggerName = context(LogAnnotation.Name)

      val maybeSpanId                 = context
        .get(Annotations.SpanIdAnnotation)
        .map("span.id" -> _.value)
      val maybeTraceId                = context
        .get(Annotations.TraceIdAnnotation)
        .map("trace.id" -> _.value)

      val maybeError                  = context
        .get(LogAnnotation.Throwable)
        .map(Cause.fail)
        .orElse(context.get(LogAnnotation.Cause))
        .map(cause => LoggingFormat.NL + cause.prettyPrint)
        .map("error.stack_Trace" -> _)

      val fields: Map[String, String] = ListMap(
        "@timestamp" -> date,
        "message"    -> line,
        "log.logger" -> loggerName,
        "log.level"  -> level
      ) ++ maybeError ++ maybeTraceId ++ maybeSpanId ++ customFields

      fields.toJson
    }
  }
}
