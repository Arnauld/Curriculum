package curriculum.util

import org.slf4j.{LoggerFactory, Logger}
import java.net.URL
import org.apache.commons.io.IOUtils
import org.apache.commons.codec.digest.DigestUtils

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

  var content: Array[Byte] = Bytes.EMPTY
  var etag: String = "0"
  var lastModified = -1L

  def outdated_? = (lastModified != URLs.lastModified(url).getOrElse(-1L))

  def refreshContentIfRequired() {
    if (outdated_?) {
      content = URLs.toBytes(url).getOrElse(Bytes.EMPTY)
      etag = DigestUtils.md5Hex(content)
    }
  }
}