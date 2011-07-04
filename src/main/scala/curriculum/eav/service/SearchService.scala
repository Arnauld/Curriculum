package curriculum.eav.service

import curriculum.util.{AsyncResult, SearchParameters}
import curriculum.eav.Instance

trait SearchService {
  def searchBySimilitude(instance:Instance, keywords:Array[String], searchParameter:SearchParameters):SimilitudeSearch
}

trait SimilitudeSearch extends AsyncResult[List[WeightedInstance]] {
}

case class WeightedInstance(instanceId:Long, weight:Int)





