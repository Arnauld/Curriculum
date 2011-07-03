package curriculum.web

import curriculum.eav.service.{InstanceLoader, InstanceService, ModelLoader, EntityService}
import curriculum.task.TaskService
import curriculum.cluster.ClusterService
import curriculum.util.MessageQueue

trait ServicesProvider {
  /*
   * TODO ioc
   */
  val entityService = new EntityService {}
  val modelReader = new ModelLoader {
    def getEntityService = entityService
  }
  val instanceService = new InstanceService {}
  val instanceReader = new InstanceLoader {
    def getInstanceService = instanceService

    def getEntityService = entityService
  }
  val taskService = new TaskService {}
  val clusterService = new ClusterService {}

  val messageQueue = new MessageQueue {}
}