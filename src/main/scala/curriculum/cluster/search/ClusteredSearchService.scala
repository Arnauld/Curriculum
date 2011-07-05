package curriculum.cluster.search

import curriculum.util.SearchParameters
import curriculum.cluster.{ClusterJob, ClusterService}
import java.io.{DataInputStream, DataOutputStream}
import curriculum.message.MessageQueue
import curriculum.eav.{SerializerComponent, Instance}
import scala.Any
import curriculum.eav.service.{SearchBySimilitude, SearchMessage, WeightedInstance, SearchService}

trait ClusteredSearchService extends SearchService {
  self:ClusterService =>

  def search(criteria:SearchBySimilitude) = {
    MessageQueue.Local.publish(SearchMessage.searchBySimilitude(criteria.instance, criteria.keywords))

    val job = new ClusterJob with SearchBySimilitude.Result {
      private var result:Option[List[WeightedInstance]] = None
      private var callback:Option[(List[WeightedInstance]) => Any] = None

      def actionName = "searchBySimilitude"

      def readResponse(in: DataInputStream) {

      }


      def setCallback(c: (List[WeightedInstance]) => Any) {
        callback = Some(c)
      }

      def writeQuery(out: DataOutputStream) {
        criteria.writeJSON(out, Map.empty[Any,Any])
      }

      def isDone = result.isDefined

      def getResult = result
    }
    dispatchJob(job)
    job
  }
}