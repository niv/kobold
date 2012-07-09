name := "kobold"

scalacOptions += "-deprecation"

scalacOptions += "-unchecked"

resolvers += "Sonatype OSS Snapshots" at "http://repo.codahale.com/"

libraryDependencies ++= Seq(
	"org.codehaus.jackson" % "jackson-core-asl" % "1.9.7",
	"org.codehaus.jackson" % "jackson-mapper-asl" % "1.9.7",
	"com.codahale" %% "jerkson" % "0.5.0",
	"net.liftweb" %% "lift-util" % "2.4"
)
