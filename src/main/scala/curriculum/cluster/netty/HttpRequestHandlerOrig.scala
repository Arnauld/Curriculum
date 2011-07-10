package curriculum.cluster.netty

import org.jboss.netty.util.CharsetUtil
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.buffer.{ChannelBuffers, ChannelBuffer}
import org.jboss.netty.channel._
import org.slf4j.LoggerFactory

import org.jboss.netty.handler.codec.http.HttpHeaders._
import org.jboss.netty.handler.codec.http.HttpHeaders.Names._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http.HttpVersion._
import scala.collection.JavaConversions._

class HttpRequestHandlerOrig extends SimpleChannelUpstreamHandler {

  val log = LoggerFactory.getLogger(classOf[HttpRequestHandlerOrig])

  var request: HttpRequest = null
  var readingChunks: Boolean = false

  /**Buffer that stores the response content */
  private val buf = new StringBuilder

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
    if (!readingChunks) {
      this.request = e.getMessage.asInstanceOf[HttpRequest]
      val request: HttpRequest = this.request

      if (is100ContinueExpected(request)) {
        send100Continue(e);
      }

      buf.setLength(0);
      buf.append("WELCOME TO THE WILD WILD WEB SERVER\r\n");
      buf.append("===================================\r\n");

      buf.append("VERSION: " + request.getProtocolVersion + "\r\n");
      buf.append("HOSTNAME: " + getHost(request, "unknown") + "\r\n");
      buf.append("REQUEST_URI: " + request.getUri + "\r\n\r\n");

      val headers = request.getHeaders
      headers.foreach({
        h =>
          buf.append("HEADER: " + h.getKey + " = " + h.getValue + "\r\n");
      })
      buf.append("\r\n");

      val queryStringDecoder = new QueryStringDecoder(request.getUri)
      val params = queryStringDecoder.getParameters
      if (!params.isEmpty) {
        val entries = params.entrySet
        entries.foreach({
          t =>
            val key = t.getKey
            val values = t.getValue
            buf.append("PARAM: " + key + " = " + values.reduceLeft(_ + ";" + _) + "\r\n");

        })
        buf.append("\r\n");
      }

      if (request.isChunked) {
        readingChunks = true;
      } else {
        val content: ChannelBuffer = request.getContent
        if (content.readable()) {
          buf.append("CONTENT: " + content.toString(CharsetUtil.UTF_8) + "\r\n");
        }

        writeResponse(e);
      }
    } else {
      val chunk: HttpChunk = e.getMessage.asInstanceOf[HttpChunk]
      if (chunk.isLast) {
        readingChunks = false;
        buf.append("END OF CONTENT\r\n");

        val trailer: HttpChunkTrailer = chunk.asInstanceOf[HttpChunkTrailer]
        val headerNames = trailer.getHeaderNames
        if (!headerNames.isEmpty) {
          buf.append("\r\n");
          headerNames.foreach({
            name =>
              val values = trailer.getHeaders(name)
              buf.append("TRAILING HEADER: " + name + " = " + values.reduceLeft(_ + "," + _) + "\r\n");
          })
          buf.append("\r\n");
        }

        writeResponse(e);
      } else {
        buf.append("CHUNK: " + chunk.getContent.toString(CharsetUtil.UTF_8) + "\r\n");
      }
    }
  }

  private def writeResponse(e: MessageEvent) {
    // Decide whether to close the connection or not.
    val keepAlive = isKeepAlive(request);

    val resp = buf.toString()
    log.debug(resp)

    // Build the response object.
    val response: HttpResponse = new DefaultHttpResponse(HTTP_1_1, OK)
    response.setContent(ChannelBuffers.copiedBuffer(resp, CharsetUtil.UTF_8))
    response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8")

    if (keepAlive) {
      // Add 'Content-Length' header only for a keep-alive connection.
      response.setHeader(CONTENT_LENGTH, response.getContent.readableBytes)
    }

    // Encode the cookie.
    val cookieString: String = request.getHeader(COOKIE);
    if (cookieString != null) {
      val cookieDecoder = new CookieDecoder();
      val cookies = cookieDecoder.decode(cookieString);
      if (!cookies.isEmpty) {
        // Reset the cookies if necessary.
        val cookieEncoder = new CookieEncoder(true)
        cookies.foreach(cookieEncoder.addCookie(_))
        response.addHeader(SET_COOKIE, cookieEncoder.encode)
      }
    }

    // Write the response.
    val future: ChannelFuture = e.getChannel.write(response)

    // Close the non-keep-alive connection after the write operation is done.
    if (!keepAlive) {
      future.addListener(ChannelFutureListener.CLOSE)
    }
  }

  private def send100Continue(e: MessageEvent) {
    val response: HttpResponse = new DefaultHttpResponse(HTTP_1_1, CONTINUE)
    e.getChannel.write(response)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) {
    log.error("Oops", e.getCause)
    e.getChannel.close()
  }

}