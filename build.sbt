lazy val baseName  = "ScalaCollider-DOT"
lazy val baseNameL = baseName.toLowerCase

lazy val projectVersion = "1.2.0"
lazy val mimaVersion    = "1.2.0"

lazy val deps = new {
 val main = new {
   val scalaCollider = "2.2.0"
   val ugens         = "1.20.0"
  }
}

lazy val root = project.withId(baseNameL).in(file("."))
  .settings(
    name               := baseName,
    version            := projectVersion,
    organization       := "de.sciss",
    description        := "Utility for exporting a ScalaCollider UGen Graph as GraphViz .dot file",
    homepage           := Some(url(s"https://git.iem.at/sciss/${name.value}")),
    licenses           := Seq("AGPL v3+" -> url("http://www.gnu.org/licenses/agpl-3.0.txt")),
    scalaVersion       := "2.13.3",
    crossScalaVersions := Seq("3.0.0-M1", "2.13.3", "2.12.12"),
    mimaPreviousArtifacts := Set(organization.value %% baseNameL % mimaVersion),
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-encoding", "utf8", "-Xlint", "-Xsource:2.13"),
    scalacOptions ++= {
      val sv = scalaVersion.value
      if (sv.startsWith("2.13.")) "-Wvalue-discard" :: Nil else Nil
    },
    libraryDependencies ++= Seq(
      "de.sciss" %% "scalacollider"           % deps.main.scalaCollider,
      "de.sciss" %  "scalacolliderugens-spec" % deps.main.ugens
    )
  )
  .settings(publishSettings)

// ---- publishing ----
lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishTo := {
    Some(if (isSnapshot.value)
      "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    else
      "Sonatype Releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
    )
  },
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  pomExtra := { val n = name.value
<scm>
  <url>git@git.iem.at:sciss/{n}.git</url>
  <connection>scm:git:git@git.iem.at:sciss/{n}.git</connection>
</scm>
<developers>
  <developer>
    <id>sciss</id>
    <name>Hanns Holger Rutz</name>
    <url>http://www.sciss.de</url>
  </developer>
</developers>
  }
)
