import AssemblyKeys._

name := "kobold"

scalacOptions += "-deprecation"

scalacOptions += "-unchecked"

resolvers += "Sonatype OSS Snapshots" at "http://repo.codahale.com/"

resolvers ++= Seq(
	"snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
	"releases" at "http://oss.sonatype.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
	"commons-configuration" % "commons-configuration" % "1.8",
	"org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
	"com.codahale" %% "logula" % "2.1.3"
	//"org.specs2" %% "specs2" % "1.11" % "test"
)

assemblySettings
