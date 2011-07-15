package curriculum.cluster.zookeeper

import org.apache.zookeeper.server.persistence.FileTxnSnapLog
import java.io.File
import org.apache.zookeeper.server.{NIOServerCnxn, ZooKeeperServer, ServerConfig}
import org.slf4j.LoggerFactory

class ZookeeperEmbeddedServer {
  private val log = LoggerFactory.getLogger(classOf[ZookeeperEmbeddedServer])

  var zkServerOpt: Option[ZooKeeperServer] = None
  var cnxnFactoryOpt: Option[NIOServerCnxn.Factory] = None

  def start(config: ServerConfig) {
    log.info("Starting Zookeeper")
    // create a file logger url from the command line args
    val ftxn: FileTxnSnapLog = new FileTxnSnapLog(new File(config.getDataLogDir), new File(config.getDataDir))
    val zkServer: ZooKeeperServer = new ZooKeeperServer
    zkServer.setTxnLogFactory(ftxn)
    zkServer.setTickTime(config.getTickTime)
    zkServer.setMinSessionTimeout(config.getMinSessionTimeout)
    zkServer.setMaxSessionTimeout(config.getMaxSessionTimeout)
    zkServerOpt = Some(zkServer)

    val cnxnFactory = new NIOServerCnxn.Factory(config.getClientPortAddress, config.getMaxClientCnxns)
    cnxnFactoryOpt = Some(cnxnFactory)
    log.info("Starting connection factory")
    cnxnFactory.startup(zkServer)
    log.info("Zookeeper server started")
  }

  def awaitTermination() {
    cnxnFactoryOpt.foreach(_.join())
    log.info("Zookeeper server connection listening stoped")
  }

  /**
   * Shutdown the serving instance
   */
  def shutdown() {
    log.info("Zookeeper server shutdown invoked")
    cnxnFactoryOpt.foreach(_.shutdown())
    zkServerOpt.foreach({
      zk => if (zk.isRunning) zk.shutdown()
    })
  }

}