organization := "fi.zapzap"
name := "timo-starter"
version := "1.7"

scalaVersion := "2.13.4"
// For Settings/Task reference, see http://www.scala-sbt.org/release/sxr/sbt/Keys.scala.html

lazy val scalaTestVersion = "3.2.0-M2"

val ZioVersion   = "1.0.12"
val slf4jVersion = "1.7.32"

libraryDependencies ++= Seq(
  "dev.zio"              %% "zio"                      % "1.0.11",
  //"dev.zio" %% "zio-streams" % "1.0.8",
  "io.github.kitlangton" %% "zio-magic"                % "0.3.5",
  "dev.zio"              %% "zio-config"               % "1.0.10",
  "dev.zio" %% "zio-config-typesafe" % "1.0.10",
  "dev.zio"              %% "zio-cache"                % "0.1.0",
  "dev.zio"              %% "zio-json"                 % "0.1.5",
  "ch.qos.logback"        % "logback-classic"          % "1.2.6",
  "net.logstash.logback"  % "logstash-logback-encoder" % "6.6",
  "dev.zio"              %% "zio-logging-slf4j"        % "0.5.12",
  "org.slf4j"             % "slf4j-api"                % slf4jVersion,
  "io.d11"               %% "zhttp"                    % "1.0.0.0-RC17",
  "org.scalatest"        %% "scalatest-freespec"       % scalaTestVersion % "test",
  "org.scalatest"        %% "scalatest-mustmatchers"   % scalaTestVersion % "test",
  "org.scalacheck"       %% "scalacheck"               % "1.14.2"         % "test"
)

testOptions in Test += Tests.Argument(
  TestFrameworks.ScalaCheck,
  "-maxSize",
  "5",
  "-minSuccessfulTests",
  "33",
  "-workers",
  s"${java.lang.Runtime.getRuntime.availableProcessors - 1}",
  "-verbosity",
  "1"
)
