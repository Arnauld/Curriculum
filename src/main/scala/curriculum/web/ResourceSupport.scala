package curriculum.web

import java.util.concurrent.ConcurrentHashMap
import java.net.{HttpURLConnection, URL}
import curriculum.util.{Bytes, URLResource}
import org.scalatra.ScalatraKernel
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Simple trait to manage static webapp resources with a really simple and basic cache management.
 */
trait ResourceSupport extends ScalatraKernel {
  self: {
    def log: org.slf4j.Logger
  } =>

  private val resourceCache = new ConcurrentHashMap[String, URLResource]

  private def formatTS(ts: Long) =
  // note: dateformat is not thread safe, thus to keep it simple one create a new one each time
    new SimpleDateFormat("yyyy/MM/dd HH:mm:ss-SSS").format(new Date(ts))

  /**
   *
   */
  protected def getResource(resourcePath: String, contentType: String): Array[Byte] = {
    log.debug("Loading resource <{}>", resourcePath)
    resourceCache.get(resourcePath) match {
      case res: URLResource =>
        handleResource(res, contentType)
      case _ =>
        /*
         * not yet in cache or missing resource
         */
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
    /*
     * Test if resources has been changed in the meanwhile
     */
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