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
import com.google.common.collect.{Multimap, LinkedListMultimap}
import curriculum.eav.service.WeightedInstance
import curriculum.util.{ToJSON, Misc}
import Misc._

class HttpRequestHandler extends SimpleChannelUpstreamHandler {

  val log = LoggerFactory.getLogger(classOf[HttpRequestHandler])

  var request: HttpRequest = null
  var readingChunks: Boolean = false

  val headers:Multimap[String,String] = LinkedListMultimap.create[String,String]()
  var protocolVersion: Option[HttpVersion] = None
  var host:Option[String] = None
  var requestUri:Option[String] = None
  var path:Option[String] = None
  var content:Array[Byte] = Array.empty[Byte]

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
    if (!readingChunks) {
      this.request = e.getMessage.asInstanceOf[HttpRequest]
      val request: HttpRequest = this.request

      if (is100ContinueExpected(request)) {
        send100Continue(e);
      }

      protocolVersion = Some(request.getProtocolVersion)
      host = someOrNone(getHost(request))
      requestUri = someOrNone(request.getUri)

      request.getHeaders.foreach({ h => headers.put(h.getKey, h.getValue) })
      val queryStringDecoder = new QueryStringDecoder(request.getUri)
      import scala.collection.JavaConversions._
      queryStringDecoder.getParameters.foreach({t => headers.putAll(t._1, t._2) })
      path = Some(queryStringDecoder.getPath)

      if (request.isChunked) {
        readingChunks = true;
      } else {
        val content: ChannelBuffer = request.getContent
        if (content.readable()) {
          this.content = content.array()
        }

        writeResponse(e)
      }
    } else {
      val chunk: HttpChunk = e.getMessage.asInstanceOf[HttpChunk]
      if (chunk.isLast) {
        readingChunks = false

        val trailer: HttpChunkTrailer = chunk.asInstanceOf[HttpChunkTrailer]
        val headerNames = trailer.getHeaderNames
          headerNames.foreach({
            name =>
              val values = trailer.getHeaders(name)
              headers.putAll(name, values)
          })

        writeResponse(e)
      } else {
        this.content = chunk.getContent.array()
      }
    }
  }

  private def writeResponse(e: MessageEvent) {
    // Decide whether to close the connection or not.
    val keepAlive = isKeepAlive(request);

    log.debug(new String(content, CharsetUtil.UTF_8))

    val reponseContent = path match {
      case Some(query) if(query=="/searchBySimilitude") =>
        val found = new WeightedInstance(1, 100)
        ToJSON.toJson(Array(found))
      case Some(query) =>
        log.warn("Unsupported query <{}>", query)
        Array.empty[Byte]
      case _ =>
        log.error("No query action defined")
        Array.empty[Byte]
    }

    // Build the response object.
    val response: HttpResponse = new DefaultHttpResponse(HTTP_1_1, OK)
    response.setContent(ChannelBuffers.copiedBuffer(reponseContent))
    response.setHeader(CONTENT_TYPE, "text/json; charset=UTF-8")

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