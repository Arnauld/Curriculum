package curriculum.util

import java.net.URL
import org.apache.commons.io.IOUtils
import java.io.{DataOutputStream, OutputStream}

object Bytes {
  val EMPTY = Array[Byte]()

  def toBytes(url: URL) = {
    val input = url.openStream
    try {
      IOUtils.toByteArray(input)
    }
    finally {
      IOUtils.closeQuietly(input)
    }
  }

}