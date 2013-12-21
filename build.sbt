import spray.revolver.RevolverPlugin._

name := "gBridge"

version := "0.1"

scalaVersion := "2.10.3"

seq(Revolver.settings: _*)

resolvers += "Sonatype (releases)" at "https://oss.sonatype.org/content/repositories/releases/"

libraryDependencies ++= {
  val akkaV = "2.2.3"
  Seq(
    "org.scalatest"     %  "scalatest_2.10"       % "2.0" % "test",
    "org.scalautils"    %  "scalautils_2.10"      % "2.0",
    "io.spray"          %% "spray-json"           % "1.2.5",
    "com.typesafe.akka" %% "akka-actor"           % akkaV,
    "com.typesafe.akka" %% "akka-testkit"         % akkaV,
    "com.typesafe.akka" %% "akka-slf4j"           % akkaV,
    "ch.qos.logback"    %  "logback-classic"      % "1.0.13",
    "org.zeromq"        %  "jzmq"                 % "2.2.2"
  )
}
