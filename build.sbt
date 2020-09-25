import sbt.plugins.SbtPlugin

name := "sbt-git-versioning"
organization := "com.hiya"

licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT"))

bintrayOrganization := Some("rallyhealth")
bintrayRepository := "sbt-plugins"

// SbtPlugin requires sbt 1.2.0+
// See: https://developer.lightbend.com/blog/2018-07-02-sbt-1-2-0/#sbtplugin-for-plugin-development
enablePlugins(SbtPlugin)

scalacOptions ++= {
  val linting = CrossVersion.partialVersion(scalaVersion.value) match {
    // some imports are necessary for compat with 2.10.
    // 2.12 needs to chill out with the unused imports warnings.
    case Some((2, 12)) => "-Xlint:-unused,_"
    case _ => "-Xlint"
  }
  Seq("-Xfatal-warnings", linting)
}

crossSbtVersions := List("1.2.8")

publishMavenStyle := true

//resolvers += Resolver.bintrayRepo("typesafe", "sbt-plugins")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "se.sawano.java" % "alphanumeric-comparator" % "1.4.1"
)

// disable scaladoc generation
sources in(Compile, doc) := Seq.empty

publishArtifact in packageDoc := false

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
bintrayRepository := "maven"
bintrayOrganization := Some("hiyainc-oss")
bintrayReleaseOnPublish in ThisBuild := false
resolvers += Resolver.bintrayRepo("hiyainc-oss", "maven")
bintrayPackageLabels := Seq("scala", "sbt")
