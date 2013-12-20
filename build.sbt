name := "gBridge"

version := "0.1"

scalaVersion := "2.10.3"

libraryDependencies ++= {
  val akkaV = "2.2.3"
  Seq(
    "org.scalatest"     %  "scalatest_2.10"  % "2.0" % "test",
    "org.scalautils"    %  "scalautils_2.10" % "2.0",
    "io.spray"          %% "spray-json"      % "1.2.5",
    "com.typesafe.akka" %% "akka-actor"      % akkaV,
    "com.typesafe.akka" %% "akka-testkit"    % akkaV,
    "com.typesafe.akka" %% "akka-slf4j"      % akkaV
  )
}
