package curriculum.cluster

import org.slf4j.LoggerFactory
import org.apache.commons.httpclient.methods.{ByteArrayRequestEntity, PostMethod}
import org.apache.commons.httpclient.{HttpException, HttpStatus, HttpClient}
import java.io._

private class Http

object Http {
  val log = LoggerFactory.getLogger(classOf[Http])

  val client = new HttpClient

  def post(jobId:Long, node:ClusterNode, job:ClusterJob) {
    val method = new PostMethod("http://"+node.address+":"+node.port+"/"+job.actionName)
    method.addRequestHeader("job_id", jobId.toString)
    method.addRequestHeader("response_path", "/"+job.actionName)

    val bout = new ByteArrayOutputStream()
    val dout = new DataOutputStream(bout)
    job.writeQuery(dout)
    method.setRequestEntity(new ByteArrayRequestEntity(bout.toByteArray))

    try {
      // Execute the method.
      val statusCode = client.executeMethod(method);

      if (statusCode != HttpStatus.SC_OK) {
        log.error("Method failed: " + method.getStatusLine)
      }

      // Read the response body.
      val responseBody:Array[Byte] = method.getResponseBody

      // Deal with the response.
      // Use caution: ensure correct character encoding and is not binary data
      if(log.isDebugEnabled)
        log.debug(new String(responseBody));
      job.readResponse(new DataInputStream(new ByteArrayInputStream(responseBody)))

    } catch {
      case e:HttpException =>
        log.error("Fatal protocol violation: " + e.getMessage, e)
      case e:IOException =>
        log.error("Fatal transport error: " + e.getMessage, e)
    } finally {
      // Release the connection.
      method.releaseConnection()
    }
  }
}