package curriculum.web

import javax.servlet.ServletContext
import java.util.concurrent.ConcurrentHashMap
import java.net.{HttpURLConnection, URL}
import curriculum.util.{Bytes, URLResource}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.scalatra.ScalatraKernel

trait ResourceSupport extends ScalatraKernel {
	self: {
		  def log:org.slf4j.Logger
  		} =>

  private val resourceCache = new ConcurrentHashMap[String, URLResource]
  /**
   *
   */
  protected def getResource(resourcePath: String, contentType: String): Array[Byte] = {
    log.info("Loading resource <" + resourcePath + ">")
    resourceCache.get(resourcePath) match {
      case res: URLResource => handleResource(res, contentType)
      case _ =>
        // not yet in cache or missing resource
        servletContext.getResource(resourcePath) match {
          case url: URL =>
            val res = new URLResource(url)
            resourceCache.put(resourcePath, res)
            handleResource(res, contentType)
          case _ =>
            log.warn("Resource <" + resourcePath + "> not found")
            response.setStatus(HttpURLConnection.HTTP_NOT_FOUND)
            Bytes.EMPTY
        }
    }
  }

  val NOT_MODIFIED = "not_modified".getBytes

  protected def handleResource(res: URLResource, contentType: String): Array[Byte] = {
    res.refreshContentIfRequired()
    val reqEtag = request.getHeader("If-None-Match")
    if (reqEtag == res.etag) {
      response.setStatus(HttpURLConnection.HTTP_NOT_MODIFIED)
      NOT_MODIFIED
    }
    else {
      response.setContentType(contentType)
      response.addHeader("Etag", res.etag)
      response.addDateHeader("Last-Modified", res.lastModified)
      res.content
    }
  }
}