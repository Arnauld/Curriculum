package curriculum.cluster.zookeeper

import org.specs.Specification
import org.slf4j.LoggerFactory
class ZookeeperSpecs extends Specification with ZookeeperSpecsSupport {
  val log = LoggerFactory.getLogger(classOf[ZookeeperSpecs])
  var connectionOpt:Option[Connection] = None

  "Zookeeper" should {
    doBefore({
      resetZookeeperData()
      startZookeeperServer()
    })
    doAfter({
      connectionOpt.foreach(_.dispose())
      stopZookeeperServer()
    })
    "manage group easily" in {
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