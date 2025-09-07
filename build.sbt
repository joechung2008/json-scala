ThisBuild / scalaVersion := "3.7.2"

lazy val root = (project in file("."))
  .aggregate(lib, cli)

lazy val lib = (project in file("lib"))
  .settings(
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.18" % Test
  )

lazy val cli = (project in file("cli"))
  .dependsOn(lib)
  .settings(
    mainClass := Some("com.github.joechung2008.jsonscala.cli.Main")
  )
