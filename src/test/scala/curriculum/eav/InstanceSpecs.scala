package curriculum.eav

import org.specs.Specification
import service.{InstanceService, EntityService}

class InstanceSpecs extends Specification {

  val civilityJSON = """ {"entity":"civility","attributes":{"first_name":"Sherlock","last_name":"Holmes"}} """.trim()

  "Instance" should {
    "serialize itself into JSON when no values are defined" in {
      val s = Samples.civility.newInstance
      s.toJSONString() must_== """ {"entity":"civility","attributes":{}} """.trim()
    }
    
    "serialize itself into JSON with primitive values" in {
      val s = Samples.sherlock_civility
      s.toJSONString() must_== civilityJSON
    }

    "serialize itself into JSON with entities values" in {
      val s = Samples.sherlock
      val actual = s.toJSONString()
      val expected = """{"entity":"person","attributes":{"civility":"""+civilityJSON+""","hobbies":["Anatomy","Chemistry","Violin","Sensational Literature"]}}""".trim()
      println("actual......["+actual+"]")
      println("expected....["+expected+"]")
      actual must_== expected
    }

    "be construct from simple JSON" in {
      val entityService = new EntityService {}
      val instanceService = new InstanceService {}
      entityService.declare(Samples.civility)
      entityService.declare(Samples.person)

      val inst = Instance.readFromJSON(
        """ {"entity":"civility","attributes":{}} """.trim(),
        instanceService,
        entityService).get
      inst.entity.entityName must_== "civility"
    }

  }
}