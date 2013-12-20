name := "PDC"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  javaJdbc,
  javaEbean,
  cache
)     

play.Project.playJavaSettings
