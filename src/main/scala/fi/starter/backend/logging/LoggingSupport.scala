package fi.starter.backend.logging

import zio.ZLayer
import zio.clock.Clock
import zio.config._
import ConfigDescriptor._
import fi.starter.backend.logging.LoggingFormat.EcsJsonFormat
import zio.config.read
import zio.logging.LogAppender.withLoggerNameFromLine
import zio.logging.Logging.{addTimestamp, modifyLoggerM}
import zio.logging.{LogAppender, LogFormat, LogLevel, Logging}

object LoggingSupport {
  private val configDescriptor: ConfigDescriptor[(Option[String], Option[Boolean])] =
    (string("LOG_LEVEL_ROOT").optional |@| boolean("LOG_SIMPLE_FORMAT").optional).tupled

  private val readConfig =
    ConfigSource
      .fromSystemProps
      .map(source =>
        read(configDescriptor from source)
          .toOption
          .map(config =>
            LoggingConfig(
              toLogLevel(config._1),
              toLogFormat(config._2)
            )
          )
          .getOrElse(LoggingConfig(defaultLevel, defaultFormat))
      )

  private def toLogLevel(level: Option[String]) = level match {
    case Some("trace") => LogLevel.Trace
    case Some("debug") => LogLevel.Debug
    case Some("info") => LogLevel.Info
    case Some("warn") => LogLevel.Warn
    case Some("error") => LogLevel.Error
    case Some("fatal") => LogLevel.Fatal
    case Some("off") => LogLevel.Off
    case _ => defaultLevel
  }

  private def toLogFormat(useSimpleFormat: Option[Boolean]) = useSimpleFormat match {
    case Some(value) =>
      if (value) LogFormat.ColoredLogFormat() else defaultFormat
    case _ =>
      defaultFormat
  }

  val logLayer =
    readConfig
      .toLayer
      .flatMap(config =>
        ZLayer.requires[Clock] ++
          (LogAppender.console[String](
            config.get.rootLevel,
            config.get.loggingFormat
          ) >>> withLoggerNameFromLine[String]) >+> Logging.make >>> modifyLoggerM(addTimestamp[String])
      )

  val defaultLevel = LogLevel.Info
  val defaultFormat = EcsJsonFormat(Map("event.dataset" -> "logging.log", "event.kind" -> "event"))

  final case class LoggingConfig(rootLevel: LogLevel, loggingFormat: LogFormat[String])
}
