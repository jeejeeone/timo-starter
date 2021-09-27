package fi.starter.backend

import zio.Cause
import zio.logging.{ LogAnnotation, LogContext, LogFormat }
import zio.json.EncoderOps
import scala.collection.immutable.ListMap

object LoggingFormat {
  private val NL = System.lineSeparator()

  val defautJsonFormat = EcsJsonFormat(Map("event.dataset" -> "logging.log", "event.kind" -> "event"))

  final case class EcsJsonFormat(customFields: Map[String, String] = Map()) extends LogFormat[String] {
    override def format(context: LogContext, line: String): String = {
      val date       = context(LogAnnotation.Timestamp)
      val level      = context(LogAnnotation.Level)
      val loggerName = context(LogAnnotation.Name)

      val maybeSpanId                 = context
        .get(LoggingSupport.SpanIdAnnotation)
        .map("span.id" -> _)
      val maybeTraceId                = context
        .get(LoggingSupport.TraceIdAnnotation)
        .map("trace.id" -> _)

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
