package curriculum.util

import org.slf4j.{LoggerFactory, Logger}
import java.net.URL
import org.apache.commons.io.IOUtils
import org.apache.commons.codec.digest.DigestUtils
import java.text.SimpleDateFormat
import java.util.Date

private class URLs

object URLs {
  val logger: Logger = LoggerFactory.getLogger(classOf[URLs])


  def toBytes(url: URL): Option[Array[Byte]] =
    if (url == null)
      None
    else {
      val input = url.openStream
      try {
        Some(IOUtils.toByteArray(input))
      }
      catch {
        case e =>
          logger.info("Failed to retrieve content for url <" + url + ">", e)
          None
      }
      finally {
        IOUtils.closeQuietly(input)
      }
    }

  def lastModified(url: URL): Option[Long] =
    if (url == null)
      None
    else {
      try {
        Some(url.openConnection.getLastModified)
      }
      catch {
        case e =>
          logger.info("Failed to retrieve lastModified for url <" + url + ">", e)
          None
      }
    }
}

class URLResource(val url: URL) {
  val logger: Logger = LoggerFactory.getLogger(classOf[URLResource])


  var content: Array[Byte] = Bytes.EMPTY
  var etag: String = "0"
  var lastModified = -1L

  def outdated_? = {
    val resTS = URLs.lastModified(url).getOrElse(-1L)
    logger.debug("Resource lastModified <{}> vs cache <{}> for <"+url.toString+">", formatTS(resTS), formatTS(lastModified))
    lastModified != resTS
  }

  def refreshContentIfRequired() {
    if (outdated_?) {
      logger.debug("Reloading resource content for <{}>", url.toString)
      lastModified = URLs.lastModified(url).getOrElse(-1L)
      content = URLs.toBytes(url).getOrElse(Bytes.EMPTY)
      etag = DigestUtils.md5Hex(content)
    }
  }

  private def formatTS(ts:Long) =
    // note: dateformat is not thread safe, thus to keep it simple one create a new one each time
    new SimpleDateFormat("yyyy/MM/dd HH:mm:ss-SSS").format(new Date(ts))

}