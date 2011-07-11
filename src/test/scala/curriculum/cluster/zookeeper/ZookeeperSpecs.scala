package curriculum.cluster.zookeeper

import org.specs.Specification
import org.slf4j.LoggerFactory
import java.util.Properties
import org.apache.zookeeper.server.{ZooKeeperServerMain, ServerConfig}
import org.apache.zookeeper.server.quorum.{QuorumPeerConfig}
import curriculum.util.Disposable

class ZookeeperSpecs extends Specification {
  val log = LoggerFactory.getLogger(classOf[ZookeeperSpecs])

  val props = new Properties()
  props.setProperty("tickTime", 2000.toString)
  props.setProperty("dataDir", "target/zookeeper")
  props.setProperty("clientPort", 2181.toString)
  val qConfig = new QuorumPeerConfig
  qConfig.parseProperties(props)
  val sConfig = new ServerConfig()
  sConfig.readFrom(qConfig)

  var server: Option[ZooKeeperServerMain with Disposable] = None
  var serverThread: Option[Thread] = None
  var connection: Option[Connection] = None

  def startServer() {
    val thread = new Thread(new Runnable {
      def run() {
        log.info("Starting zookeeper")
        val server = new ZooKeeperServerMain() with Disposable {
          def dispose() {
            super.shutdown()
          }
        }
        server.runFromConfig(sConfig)
      }
    })
    serverThread = Some(thread)
    thread.start()
  }

  "Zookeeper" should {
    doAfter({
      connection.foreach(_.dispose())
      server.foreach(_.dispose())
      serverThread.foreach(_.interrupt())
    })
    "be startable as standalone" in {
      startServer()
      
      val groupName = "travis"

      val service = new Connection with GroupService
      service.connect("localhost")
      service.createGroup(groupName)
      service.joinGroup(groupName, "vlad")
      service.joinGroup(groupName, "thundercat")
      val members = service.listGroupMembers("travis")
      service.deleteGroup(groupName)
      members must_== Array("thundercat", "vlad")
    }
    "manage queue easily" in {
      startServer()

      val service = new Connection with QueueService with ZookeeperSupport
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