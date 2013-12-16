import org.codeck.play2sae.sbtPlugin //this line should be add on top

name := "PDC"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache
)     

play.Project.playJavaSettings

resolvers += Resolver.url("codeck repo", url("https://github.com/codeck/play2sae/raw/ivy-repo/"))(Resolver.ivyStylePatterns)

sbtPlugin.saeSettings
