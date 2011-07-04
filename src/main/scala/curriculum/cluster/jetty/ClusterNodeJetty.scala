package curriculum.cluster.jetty

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{FilterHolder, ServletContextHandler}
import curriculum.web.CurriculumFilter
import org.slf4j.LoggerFactory
import java.net.URL
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.util.resource.Resource
import org.eclipse.jetty.server.nio.SelectChannelConnector
import curriculum.cluster.ClusterNode
import java.lang.IllegalArgumentException

object ClusterNodeJetty {
}

case class ClusterNodeJetty(node: ClusterNode) {
  val name: String = node.name
  val port: Int = node.port
  val webapp = node.parameters.get("webapp-location").getOrElse({
    throw new IllegalArgumentException("Missing parameter 'webapp-location'")
  }).asInstanceOf[URL]

  val log = LoggerFactory.getLogger(classOf[ClusterNodeJetty])

  private var serverOpt: Option[Server] = None

  var useWebAppContext = true

  def start() {
    synchronized {
      serverOpt match {
        case None =>

          val context = if (useWebAppContext) {
            val descriptorLocation = webapp.getFile + "WEB-INF/web.xml"
            log.debug("Node <{}> - descriptor location: <{}>", name, descriptorLocation)
            val c = new WebAppContext {
              override def getResource(uriInContext: String) = {
                log.debug("Attempt to getResource in context <{}> according to base <{}>", uriInContext, getBaseResource.getFile)
                val res = super.getResource(uriInContext).asInstanceOf[Resource]
                log.debug("Got: <{}>", res)
                res
              }
            }
            c.setDescriptor(descriptorLocation)
            c.setResourceBase(webapp.getFile)
            c.setClassLoader(Thread.currentThread.getContextClassLoader);
            c.setContextPath("/")
            c
          }
          else {
            val c = new ServletContextHandler(ServletContextHandler.SESSIONS)
            c.setContextPath("/")
            c.setResourceBase(".")
            c.setClassLoader(Thread.currentThread.getContextClassLoader);
            c.addFilter(new FilterHolder(new CurriculumFilter), "/*", org.eclipse.jetty.servlet.FilterMapping.DEFAULT)
            c
          }

          val defaultConnector = new SelectChannelConnector
          defaultConnector.setPort(port)
          defaultConnector.setMaxIdleTime(30000)

          val server = new Server()
          server.addConnector(defaultConnector)
          server.setHandler(context)
          server.start()

          serverOpt = Some(server)
          log.info("Node <{}> started on port {}", name, port)

        case Some(s) =>
          log.info("Node <{}> already started on port {}", name, port)
      }
    }
  }
}