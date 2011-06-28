package curriculum.eav.service

import org.specs.Specification
import curriculum.domain.CurriculumVitaeModels
import curriculum.eav.{Attribute, Entity}
import org.slf4j.LoggerFactory

class ModelLoaderSpecs extends Specification {

  val _logger = LoggerFactory.getLogger(classOf[ModelLoaderSpecs])

  "Model loader" should {
    "be able to parse basic definitions" in {
      val models = CurriculumVitaeModels.Models
      _logger.debug(models.toString())
      
      val entityService = new EntityService {}
      val metaModelReader = new ModelLoader {
        def getEntityService = entityService
        def log = LoggerFactory.getLogger("curriculum.eav.service.ModelLoader")
      }

      metaModelReader.load(models)
      entityService.getEntities.foreach({ e =>
        val (name,entity) = e
        _logger.debug(name)
        entity.getAttributes.foreach({a =>
          val (name,attr) = a
          _logger.debug("  " + name + ": " + attr.dataType)
          _logger.debug("  " + attr.getHtmlDescriptions)
        })
      })
    }
  }
}