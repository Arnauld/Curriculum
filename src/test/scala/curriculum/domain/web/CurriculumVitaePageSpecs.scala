package curriculum.domain.web

import org.specs.Specification
import org.slf4j.LoggerFactory
import curriculum.eav.service.{InstanceLoader, InstanceService, ModelLoader, EntityService}
import curriculum.domain.{CurriculumVitaeInstances, CurriculumVitaeModels}
import org.apache.commons.io.IOUtils
import java.io.FileOutputStream

class CurriculumVitaePageSpecs extends Specification {

  val _logger = LoggerFactory.getLogger(classOf[CurriculumVitaePageSpecs])

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

  "Curriculum Page" should {
    "be produced correctly" in {
      /*
       * load instance
       */
      modelReader.load(CurriculumVitaeModels.Models)
      val male = instanceReader.loadInstance(CurriculumVitaeInstances.GenreMale)
      val female = instanceReader.loadInstance(CurriculumVitaeInstances.GenreFemale)
      instanceService.register(male)
      instanceService.register(female)

      val instance = instanceReader.loadInstance(CurriculumVitaeInstances.Arnauld)

      /*
       * generate page
       */
      val page = new CurriculumVitaePage(instance)
      val html = page.content
      _logger.info(page.bodyContent.toString())
      IOUtils.write(page.bodyContent.toString(), new FileOutputStream("/Users/arnauld/Projects/curriculum/target/data.xml"), "utf-8")
    }
  }
}