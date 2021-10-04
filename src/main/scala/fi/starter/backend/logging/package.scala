package fi.starter.backend

import org.slf4j.MDC
import zio.ZIO
import zio.logging.{ Logging, log }

import scala.jdk.CollectionConverters.MapHasAsJava

package object logging {
  def loggedEffect[A](fn: => A): ZIO[Logging, Throwable, A] =
    log.context.flatMap { context =>
      ZIO.effect {
        val mdc: Map[String, String] = context.renderContext
        val previous                 = Option(MDC.getCopyOfContextMap()).getOrElse(Map.empty[String, String].asJava)

        MDC.setContextMap(mdc.asJava)

        try fn
        finally MDC.setContextMap(previous)
      }
    }
}
