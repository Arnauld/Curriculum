package curriculum.cluster.netty

import org.slf4j.LoggerFactory
import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import java.util.concurrent.Executors
import org.jboss.netty.channel.{Channels, ChannelPipeline, ChannelPipelineFactory, Channel}
import org.jboss.netty.handler.codec.http.{HttpContentCompressor, HttpResponseEncoder, HttpRequestDecoder}
import java.net.{InetSocketAddress, URL}
import curriculum.util.{MessageQueue, ProgressMonitor}
import curriculum.cluster.{ClusterMessage, ClusterNode}

object ClusterNodeNetty {
}

case class ClusterNodeNetty(node:ClusterNode) {
  val name: String = node.name
  val port: Int = node.port

  val log = LoggerFactory.getLogger(classOf[ClusterNodeNetty])

  private var channel: Option[Channel] = None

  def start(monitor:ProgressMonitor) {
    monitor.subTask("check-node-already-started")
    synchronized {
      channel match {
        case None =>
          monitor.worked(1)
          monitor.subTask("server-configuration")
          // Configure the server.
          val bootstrap = new ServerBootstrap(
            new NioServerSocketChannelFactory(
              Executors.newCachedThreadPool(),
              Executors.newCachedThreadPool()))

          monitor.worked(1)
          monitor.subTask("pipeline-factory")

          // Set up the pipeline factory.
          bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            def getPipeline: ChannelPipeline = {
              // Create a default pipeline implementation.
              val pipeline: ChannelPipeline = Channels.pipeline();

              // Uncomment the following line if you want HTTPS
              //SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
              //engine.setUseClientMode(false);
              //pipeline.addLast("ssl", new SslHandler(engine));

              pipeline.addLast("decoder", new HttpRequestDecoder)
              // Uncomment the following line if you don't want to handle HttpChunks.
              //pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
              pipeline.addLast("encoder", new HttpResponseEncoder)
              // Remove the following line if you don't want automatic content compression.
              pipeline.addLast("deflater", new HttpContentCompressor)
              pipeline.addLast("handler", new HttpRequestHandler)
              pipeline;
            }
          })

          monitor.worked(1)
          monitor.subTask("port-binding")
          // Bind and start to accept incoming connections.
          channel = Some(bootstrap.bind(new InetSocketAddress(port)))
          MessageQueue.Local.publish(ClusterMessage.nodeStarted(node))
          log.info("Node <{}> started on port {}", name, port)

        case Some(s) =>
          monitor.worked(3)
          MessageQueue.Local.publish(ClusterMessage.nodeAlreadyStarted(node))
          log.info("Node <{}> already started on port {}", name, port)
      }
    }
  }
}