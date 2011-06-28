package curriculum.eav.service

import org.specs.Specification
import org.slf4j.LoggerFactory
import curriculum.domain.{CurriculumVitaeModels, CurriculumVitaeInstances}
import com.sun.xml.internal.bind.v2.model.impl.DummyPropertyInfo
import curriculum.eav.Instance

class InstanceLoaderSpecs extends Specification {

  val _logger = LoggerFactory.getLogger(classOf[InstanceLoaderSpecs])

  val entityService = new EntityService {}
  val modelReader = new ModelLoader {
    def getEntityService = entityService

    def log = _logger
  }
  val instanceService = new InstanceService {}
  val instanceReader = new InstanceLoader {
    def getInstanceService = instanceService

    def getEntityService = entityService

    def log = _logger
  }


  "Instance loader" should {
    "be able to parse basic instance" in {
      modelReader.load(CurriculumVitaeModels.Models)
      val male = instanceReader.loadInstance(CurriculumVitaeInstances.GenreMale)
      dumpInstance(0, male)
    }

    "be able to parse more complex instance" in {
      modelReader.load(CurriculumVitaeModels.Models)
      val male = instanceReader.loadInstance(CurriculumVitaeInstances.GenreMale)
      val female = instanceReader.loadInstance(CurriculumVitaeInstances.GenreFemale)
      instanceService.register(male)
      instanceService.register(female)

      val instance = instanceReader.loadInstance(CurriculumVitaeInstances.Arnauld)
      dumpInstance(0, instance)
    }
  }

  def dumpInstance(indent: Int, instance: Instance) {
    var prefix = "  " * indent
    print(prefix)
    println("Instance of type <" + instance.entity.entityName + ">")
    prefix = "  " * (indent+1)
    instance.getAttributeValues.foreach({
      attr =>
        print(prefix + attr._1 + ": ")
        attr._2 match {
          case inst: Instance =>
            println()
            dumpInstance(indent + 2, inst)
          case list:List[Instance] =>
            println()
            list.foreach(dumpInstance(indent + 2, _))
          case other =>
            println(other)
        }
    })
  }
}