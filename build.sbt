ThisBuild / scalaVersion := "3.8.3"
ThisBuild / crossScalaVersions := Seq("2.13.18", "3.8.3")
ThisBuild / scalacOptions += "-deprecation"

lazy val root = (project in file("."))
  .aggregate(lib, cli)

lazy val lib = (project in file("lib"))
  .settings(
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test
  )

lazy val cli = (project in file("cli"))
  .dependsOn(lib)
  .settings(
    mainClass := Some("com.github.jsonscala.cli.Main")
  )
