name := "PDC"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "postgresql" % "postgresql" % "9.3-1100.jdbc4"
)     

play.Project.playJavaSettings
