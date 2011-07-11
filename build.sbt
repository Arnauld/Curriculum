name := "curriculum"

organization := "org.technbolts"

version := "0.0.2-SNAPSHOT"

scalaVersion := "2.9.0-1"

scalacOptions ++= Seq("-unchecked", "-deprecation")

seq(WebPlugin.webSettings :_*)

retrieveManaged := true // remove this once plugins are working or i understand their layout

publishMavenStyle := true

publishTo := Some(Resolver.file("Local", Path.userHome / "Projects" / "arnauld.github.com" / "maven2" asFile)(Patterns(true, Resolver.mavenStyleBasePattern)))

javaOptions in (run) += "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

libraryDependencies ++= Seq(
  // web
  "org.scalatra" %% "scalatra" % "2.0.0-SNAPSHOT",
  "org.scalatra" %% "scalatra-specs" % "2.0.0-SNAPSHOT" % "test",
  "org.scalatra" %% "scalatra-scalate" % "2.0.0-SNAPSHOT",
  // netty
  "org.jboss.netty" % "netty" % "3.2.4.Final",
  // http client for node com'
  "commons-httpclient" % "commons-httpclient" % "3.1",
  // jetty
  "org.eclipse.jetty" % "jetty-server" % "7.4.2.v20110526" % "jetty;provided",
  "org.eclipse.jetty" % "jetty-webapp" % "7.4.2.v20110526" % "jetty;provided",
  "org.eclipse.jetty" % "jetty-servlet" % "7.4.2.v20110526" % "jetty;provided",
  "javax.servlet" % "servlet-api" % "2.5" % "provided->default",
  //misc
  "commons-io" % "commons-io" % "2.0.1",
  "commons-codec" % "commons-codec" % "1.5",
  "joda-time" % "joda-time" % "1.6.2",
  "org.codehaus.jackson" % "jackson-mapper-lgpl" % "1.8.2",
  "com.google.guava" % "guava" % "r08",
  /*"com.google.inject" % "guice" % "3.0",*/
  // logs
  "org.slf4j" % "slf4j-api" % "1.6.0",
  "org.slf4j" % "log4j-over-slf4j" % "1.6.0",
  "org.slf4j" % "jcl-over-slf4j" % "1.6.0",
  "ch.qos.logback" % "logback-classic" % "0.9.25",
  // persistence BerkeleyDB :p
  /*"com.sleepycat" % "je" % "4.0.92",*/
  //test
  "org.scala-tools.testing" %% "specs" % "1.6.8" % "test"
)

ivyXML :=
  <dependencies>
    <dependency org="org.apache.zookeeper" name="zookeeper" rev="3.3.3">
      <exclude module="jmxri"/>
      <exclude module="jmxtools"/>
      <exclude module="jms"/>
      <exclude module="commons-logging"/>
    </dependency>
  </dependencies>

resolvers ++= Seq(
  "Oracle Repo" at "http://download.oracle.com/maven",
  "Sonatype OSS" at "http://oss.sonatype.org/content/repositories/releases/",
  "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "Web plugin repo" at "http://siasia.github.com/maven2",
  "FuseSource Snapshot Repository" at "http://repo.fusesource.com/nexus/content/repositories/snapshots",
  "Arnauld" at "https://github.com/Arnauld/arnauld.github.com/raw/master/maven2",
  "java net" at "http://download.java.net/maven/2/"
)