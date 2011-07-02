name := "curriculum"

organization := "org.technbolts"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.9.0-1"

scalacOptions ++= Seq("-unchecked", "-deprecation")

seq(WebPlugin.webSettings :_*)

retrieveManaged := true // remove this once plugins are working or i understand their layout

publishMavenStyle := true

publishTo := Some(Resolver.file("Local", Path.userHome / "Projects" / "arnauld.github.com" / "maven2" asFile)(Patterns(true, Resolver.mavenStyleBasePattern)))

libraryDependencies ++= Seq(
  // web
  "org.scalatra" %% "scalatra" % "2.0.0-SNAPSHOT",
  "org.scalatra" %% "scalatra-specs" % "2.0.0-SNAPSHOT" % "test",
  "org.scalatra" %% "scalatra-scalate" % "2.0.0-SNAPSHOT",
  // netty
  "org.jboss.netty" % "netty" % "3.2.4.Final",
  // jetty
  "org.eclipse.jetty" % "jetty-server" % "7.4.2.v20110526" % "jetty;provided",
  "org.eclipse.jetty" % "jetty-webapp" % "7.4.2.v20110526" % "jetty;provided",
  "org.eclipse.jetty" % "jetty-servlet" % "7.4.2.v20110526" % "jetty;provided",
  "javax.servlet" % "servlet-api" % "2.5" % "provided->default",
  //misc
  "commons-io" % "commons-io" % "2.0.1",
  "commons-codec" % "commons-codec" % "1.5",
  "joda-time" % "joda-time" % "1.6.2",
  /*"com.google.inject" % "guice" % "3.0",*/
  // logs
  "org.slf4j" % "slf4j-api" % "1.6.0",
  "ch.qos.logback" % "logback-classic" % "0.9.25",
  // persistence BerkeleyDB :p
  /*"com.sleepycat" % "je" % "4.0.92",*/
  //test
  "org.scala-tools.testing" %% "specs" % "1.6.8" % "test"
)

resolvers ++= Seq(
  "Oracle Repo" at "http://download.oracle.com/maven",
  "Sonatype OSS" at "http://oss.sonatype.org/content/repositories/releases/",
  "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "Web plugin repo" at "http://siasia.github.com/maven2",
  "FuseSource Snapshot Repository" at "http://repo.fusesource.com/nexus/content/repositories/snapshots"
)