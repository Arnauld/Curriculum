package curriculum.cluster.zookeeper

import java.io.File
import org.apache.commons.io.FileUtils
import java.util.Properties
import org.apache.zookeeper.server.quorum.QuorumPeerConfig
import org.apache.zookeeper.server.{ZooKeeperServerMain, ServerConfig}
import curriculum.util.Disposable
import org.slf4j.LoggerFactory
import java.util.concurrent.CountDownLatch

/**
 * provides support for testing Zookeeper
 */
trait ZookeeperSpecsSupport {
  private val log = LoggerFactory.getLogger(classOf[ZookeeperSpecsSupport])

  var serverOpt: Option[ZookeeperEmbeddedServer] = None

  /**
   * Zookeeper data directory, path is based on class name to prevent
   * concurrent access to the same data dir.
   */
  def dataDir:File = new File("target/zookeeper/" + getClass.getSimpleName)

  def clientPort = 2181

  /**
   *
   */
  def resetZookeeperData() {
    // remove existing data
    if (dataDir.exists()) {
      log.warn("Removing existing zookeeper data at {}", dataDir)
      FileUtils.deleteDirectory(dataDir)
    }
  }

  def createZookeeperConfig(): ServerConfig = {
    log.info("Zookeeper data directory: {}", dataDir)
    val props = new Properties()
    props.setProperty("tickTime", 2000.toString)
    props.setProperty("dataDir", dataDir.getAbsolutePath)
    props.setProperty("clientPort", clientPort.toString)
    val qConfig = new QuorumPeerConfig
    qConfig.parseProperties(props)
    val sConfig = new ServerConfig()
    sConfig.readFrom(qConfig)
    sConfig
  }

  def stopZookeeperServer() {
    serverOpt.foreach({ zk =>
      zk.shutdown()
      zk.awaitTermination()
    })
  }

  def startZookeeperServer() {
    val server = new ZookeeperEmbeddedServer()
    serverOpt = Some(server)
    server.start(createZookeeperConfig())
  }
}