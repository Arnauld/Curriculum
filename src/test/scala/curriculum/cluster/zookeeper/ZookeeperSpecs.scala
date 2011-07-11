package curriculum.cluster.zookeeper

import org.specs.Specification
import org.slf4j.LoggerFactory
import java.util.Properties
import org.apache.zookeeper.server.{ZooKeeperServerMain, ServerConfig}
import org.apache.zookeeper.server.quorum.{QuorumPeerConfig}
import curriculum.util.Disposable
import java.io.File
import org.apache.commons.io.FileUtils

class ZookeeperSpecs extends Specification {
  val log = LoggerFactory.getLogger(classOf[ZookeeperSpecs])

  // remove existing data
  val dataDir = new File("target/zookeeper")
  if(dataDir.exists()) {
    log.warn("Removing existing zookeeper data")
    FileUtils.deleteDirectory(dataDir)
  }
  log.info("Zookeeper data directory: {}", dataDir)

  val props = new Properties()
  props.setProperty("tickTime", 2000.toString)
  props.setProperty("dataDir", dataDir.getAbsolutePath)
  props.setProperty("clientPort", 2181.toString)
  val qConfig = new QuorumPeerConfig
  qConfig.parseProperties(props)
  val sConfig = new ServerConfig()
  sConfig.readFrom(qConfig)

  var serverOpt: Option[ZooKeeperServerMain with Disposable] = None
  var serverThreadOpt: Option[Thread] = None
  var connectionOpt: Option[Connection] = None

  def startServer() {
    val thread = new Thread(new Runnable {
      def run() {
        log.info("Starting zookeeper")
        val server = new ZooKeeperServerMain() with Disposable {
          def dispose() {
            super.shutdown()
          }
        }
        serverOpt = Some(server)
        server.runFromConfig(sConfig)
      }
    })
    serverThreadOpt = Some(thread)
    thread.start()
  }

  "Zookeeper" should {
    doAfter({
      connectionOpt.foreach(_.dispose())
      serverOpt.foreach(_.dispose())
      serverThreadOpt.foreach({ t=>
        t.interrupt()
        t.join()
      })
    })
    "manage group easily" in {
      startServer()
      
      val groupName = "travis"

      val service = new Connection with GroupService with ZookeeperSupport
      connectionOpt = Some(service)
      service.connect("localhost")
      service.createGroup(groupName)
      service.joinGroup(groupName, "vlad")
      service.joinGroup(groupName, "thundercat")
      val members = service.listGroupMembers(groupName)
      service.deleteGroup(groupName)
      members must containAll(List("thundercat", "vlad"))
    }
    "manage queue easily" in {
      startServer()

      val service = new Connection with QueueService with ZookeeperSupport
      connectionOpt = Some(service)
      service.connect("localhost")
      service.createQueue("jobs")
      1.to(5).foreach({ i=>
        service.publish("jobs", "Hello! #%d".format(i).getBytes)
      })

      val data1 = service.consumeOne("jobs")
      new String(data1.get) must_== "Hello! #1"
    }
  }
}