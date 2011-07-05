package curriculum.message

import org.specs.Specification
import java.util.Locale

class MessageSpecs extends Specification {

  "Message" should {
    "be serialized to JSON when parameters are empty" in {
      val msg = Message.info(Message.code("Hello %s!"), "Sherlock")
      msg.toJSONString(Locale.FRANCE) must_== """ {"id":-1,"type":"type-info","message":"Hello Sherlock!","parameters":{}} """.trim()
    }

    "be serialized to JSON when parameters are defined" in {
      val msg = Message.info(Message.code("Hello %s!"), "Sherlock")
      msg.parameters = Map[String,Any](("instance_id" -> 17))
      msg.toJSONString(Locale.FRANCE) must_== """ {"id":-1,"type":"type-info","message":"Hello Sherlock!","parameters":{"instance_id":17}} """.trim()
    }
  }

}