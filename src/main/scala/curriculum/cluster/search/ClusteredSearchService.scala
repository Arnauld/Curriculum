package curriculum.cluster.search

import curriculum.util.SearchParameters
import curriculum.cluster.{ClusterJob, ClusterService}
import java.io.{DataInputStream, DataOutputStream}
import curriculum.message.MessageQueue
import curriculum.eav.service.{SearchMessage, WeightedInstance, SimilitudeSearch, SearchService}
import curriculum.eav.{SerializerComponent, Instance}

trait ClusteredSearchService extends SearchService {
  self:ClusterService with SerializerComponent =>

  def searchBySimilitude(instance: Instance, keywords:Array[String], searchParameter: SearchParameters) = {
    MessageQueue.Local.publish(SearchMessage.searchBySimilitude(instance, kewords))

    val job = new ClusterJob with SimilitudeSearch {
      private var result:Option[List[WeightedInstance]] = None

      def actionName = "searchBySimilitude"

      def readResponse(in: DataInputStream) {

      }

      def writeQuery(out: DataOutputStream) {
        serializer.writeInstance(instance, out)
        out.writeInt(keywords.length)
        keywords.foreach(out.writeUTF(_))
      }

      def isDone = result.isDefined

      def getResult = result
    }
    dispatchJob(job)
    job
  }
}