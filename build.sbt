name               := "ScalaCollider-DOT"
version            := "0.1.0-SNAPSHOT"
organization       := "at.iem"
description        := "Utility for exporting a ScalaCollider UGen Graph as GraphViz .dot file"
homepage           := Some(url(s"https://github.com/iem-projects/${name.value}"))
licenses           := Seq("lgpl" -> url("https://www.gnu.org/licenses/lgpl-2.1.txt"))
scalaVersion       := "2.11.8"
crossScalaVersions := Seq("2.11.8", "2.10.6")

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xfuture")

lazy val scalaColliderVersion = "1.19.0"
lazy val ugensVersion         = "1.15.0"

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
