package curriculum.web

import curriculum.task.TaskService
import curriculum.cluster.ClusterService
import curriculum.eav.service._
import curriculum.eav.Instance
import curriculum.util.SearchParameters
import curriculum.cluster.search.ClusteredSearchService
import curriculum.domain.{CurriculumVitaeInstances, CurriculumVitaeModels}
import xml.Node

trait ServicesProvider {
  /*
   * TODO ioc
   */

  private var _entityService: EntityService = null

  def entityService():EntityService = synchronized {
    if (_entityService == null) {
      _entityService = new EntityService {}

      //TODO refactor: this is really ugly! with reentrant case...
      modelReader.load(CurriculumVitaeModels.Models)
      import CurriculumVitaeInstances._
      List(GenreMale, GenreFemale).foreach({
        n: Node =>
        val instance = instanceReader.loadInstance(n)
        instanceService.register(instance)
      })
    }
    _entityService
  }

  val modelReader = new ModelLoader {
    def getEntityService = entityService()
  }
  val instanceService = new InstanceService {}
  val instanceReader = new InstanceLoader {
    def getInstanceService = instanceService

    def getEntityService = entityService()
  }
  val taskService = new TaskService {}

  val clusterService = new ClusterService with ClusteredSearchService {}

  val searchService: SearchService = clusterService
}