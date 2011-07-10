package curriculum.eav.service

import curriculum.eav.Instance
import curriculum.util.{ToJSON, AsyncResult, SearchParameters}
import org.codehaus.jackson.JsonGenerator
import curriculum.util.ToJSON._
import reflect.BeanProperty

trait SearchService {
  def search(search: SearchBySimilitude): SearchBySimilitude.Result
}

object SearchBySimilitude {

  trait Result extends AsyncResult[List[WeightedInstance]]

}

case class SearchBySimilitude(instance: Instance, keywords: Array[String], searchParameter: SearchParameters) extends ToJSON {
  def writeJSONContent(g: JsonGenerator, ctx: Map[Any, Any]) {
    writeField("instance", instance)(g) // instance is also a ToJSON ;)
    writeField("keywords", keywords)(g)
  }
}

class WeightedInstance(@BeanProperty var instanceId: Long, @BeanProperty var weight: Int)





