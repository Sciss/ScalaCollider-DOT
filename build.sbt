lazy val baseName  = "ScalaCollider-DOT"
lazy val baseNameL = baseName.toLowerCase

lazy val projectVersion = "0.7.0-SNAPSHOT"
lazy val mimaVersion    = "0.7.0"

name               := baseName
version            := projectVersion
organization       := "at.iem"
description        := "Utility for exporting a ScalaCollider UGen Graph as GraphViz .dot file"
homepage           := Some(url(s"https://github.com/iem-projects/${name.value}"))
licenses           := Seq("lgpl" -> url("https://www.gnu.org/licenses/lgpl-2.1.txt"))
scalaVersion       := "2.12.5"
crossScalaVersions := Seq("2.12.5", "2.11.12")

mimaPreviousArtifacts := Set("at.iem" %% baseNameL % mimaVersion)

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xfuture", "-Xlint")

lazy val scalaColliderVersion = "1.25.0-SNAPSHOT"
lazy val ugensVersion         = "1.18.0"

libraryDependencies ++= Seq(
  "de.sciss" %% "scalacollider"           % scalaColliderVersion,
  "de.sciss" %  "scalacolliderugens-spec" % ugensVersion
)

// ---- publishing ----

publishMavenStyle := true

publishTo :=
  Some(if (isSnapshot.value)
    "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  else
    "Sonatype Releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
  )

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := { val n = name.value
<scm>
  <url>git@github.com:iem-projects/{n}.git</url>
  <connection>scm:git:git@github.com:iem-projects/{n}.git</connection>
</scm>
<developers>
  <developer>
    <id>sciss</id>
    <name>Hanns Holger Rutz</name>
    <url>http://www.sciss.de</url>
  </developer>
</developers>
}
