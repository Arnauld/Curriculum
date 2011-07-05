package curriculum.util

import org.specs.Specification
import org.codehaus.jackson.JsonGenerator

class ToJSONSpecs extends Specification {

  "ToJSON trait" should {
    "provide string representation" in {
      val toJSON = new ToJSON {
        def writeJSONContent(g: JsonGenerator, ctx: Map[Any, Any]) {
          g.writeStringField("first_name", "Sherlock")
        }
      }
      val str = toJSON.toJSONString()
    }
  }

  "ToJSON object" should {
    "serialize list of 'ToJSON' instances" in {

    }
  }
}