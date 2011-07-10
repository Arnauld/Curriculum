package curriculum.util

import org.specs.Specification
import org.codehaus.jackson.JsonGenerator
import curriculum.eav.service.WeightedInstance

class ToJSONSpecs extends Specification {

  "ToJSON trait" should {
    "provide string representation" in {
      val toJSON = new ToJSON {
        def writeJSONContent(g: JsonGenerator, ctx: Map[Any, Any]) {
          g.writeStringField("first_name", "Sherlock")
        }
      }
      val str = toJSON.toJSONString()
      println(str)
    }
  }

  "ToJSON object" should {
    "serialize list of 'ToJSON' instances" in {
        skip("empty test :(")
    }

    "deserialize list of a single object" in {
      val json = """|[{
                    |  "weight" : 100,
                    |  "instanceId" : 1
                    |}]
                    |""".stripMargin
      val values = ToJSON.valuesFromJson(json, classOf[WeightedInstance])
      values.length must_== 1
      values(0).instanceId must_== 1
      values(0).weight must_== 100
    }
  }
}