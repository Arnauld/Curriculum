package curriculum.cluster.netty

import org.specs.Specification
import org.slf4j.LoggerFactory
import curriculum.eav.Samples
import curriculum.util.{SearchParameters, ProgressMonitor}
import curriculum.eav.service.{WeightedInstance, SearchBySimilitude}
import java.io.{DataOutputStream, DataInputStream}
import curriculum.cluster.{Http, ClusterJob, ClusterNode}
import java.util.concurrent.TimeUnit

class ClusterNodeNettySpecs extends Specification {
  val log = LoggerFactory.getLogger(classOf[ClusterNodeNetty])

  var nettyNode:ClusterNodeNetty = null

  "A cluster node based on 'netty'" should {
    doAfter({
      if(nettyNode!=null)
        nettyNode.stop().foreach(_.await(5, TimeUnit.SECONDS))
    })

    "start and accept simple query" in {
      val clusterNode = ClusterNode("netty-server", 8900, Map.empty[String, Any])
      nettyNode = new ClusterNodeNetty(clusterNode)
      nettyNode.start(new ProgressMonitor {
        override def beginTask(name: String, amount: Int) {
          log.info("Node: {}", name)
        }
      })

      // post a search
      val search = new SearchBySimilitude(Samples.sherlock, Array("detective"), new SearchParameters {})
      val job = search2job(search)
      Http.post(17, clusterNode, job)

    }
  }

  def search2job(criteria: SearchBySimilitude): ClusterJob = {
    val job = new ClusterJob with SearchBySimilitude.Result {
      private var result: Option[List[WeightedInstance]] = None
      private var callback: Option[(List[WeightedInstance]) => Any] = None

      def actionName = "searchBySimilitude"

      def readResponse(in: DataInputStream) {
      }

      def setCallback(c: (List[WeightedInstance]) => Any) {
        callback = Some(c)
      }

      def writeQuery(out: DataOutputStream) {
        criteria.writeJSON(out, Map.empty[Any, Any])
      }

      def isDone = result.isDefined

      def getResult = result
    }
    job
  }
}