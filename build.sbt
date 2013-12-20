name := "gBridge"

version := "0.1"

scalaVersion := "2.10.3"

libraryDependencies ++= {
  Seq(
    "org.scalatest"  % "scalatest_2.10"  % "2.0" % "test",
    "org.scalautils" % "scalautils_2.10" % "2.0",
    "io.spray"       %% "spray-json"     % "1.2.5"
  )
}
