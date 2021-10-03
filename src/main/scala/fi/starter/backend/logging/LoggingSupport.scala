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
  private val configDescriptor: ConfigDescriptor[(String, Boolean)] =
    (string("LOG_LEVEL_ROOT").default("info") |@| boolean("LOG_SIMPLE_FORMAT").default(false)).tupled

  private val readConfig =
    ConfigSource.fromSystemProps
      .map(source =>
        read(configDescriptor from source).toOption
          .map(config =>
            LoggingConfig(
              toLogLevel(config._1),
              if (config._2) LogFormat.ColoredLogFormat() else defaultFormat
            )
          )
          .getOrElse(LoggingConfig(defaultLevel, defaultFormat))
      )

  private def toLogLevel(level: String) = level match {
    case "trace" => LogLevel.Trace
    case "debug" => LogLevel.Debug
    case "info"  => LogLevel.Info
    case "warn"  => LogLevel.Warn
    case "error" => LogLevel.Error
    case "fatal" => LogLevel.Fatal
    case "off"   => LogLevel.Off
    case _       => defaultLevel
  }

  val logLayer =
    readConfig.toLayer
      .flatMap(config =>
        ZLayer.requires[Clock] ++
          (LogAppender.console[String](
            config.get.rootLevel,
            config.get.loggingFormat
          ) >>> withLoggerNameFromLine[String]) >+> Logging.make >>> modifyLoggerM(addTimestamp[String])
      )

  val defaultLevel  = LogLevel.Info
  val defaultFormat = EcsJsonFormat(Map("event.dataset" -> "logging.log", "event.kind" -> "event"))

  final case class LoggingConfig(rootLevel: LogLevel, loggingFormat: LogFormat[String])
}
