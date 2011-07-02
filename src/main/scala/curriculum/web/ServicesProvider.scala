package curriculum.web

import javax.servlet.Filter
import curriculum.eav.service.{InstanceLoader, InstanceService, ModelLoader, EntityService}
import curriculum.task.TaskService
import curriculum.cluster.ClusterService

/**
 * Created by IntelliJ IDEA.
 * User: arnauld
 * Date: 02/07/11
 * Time: 23:44
 * To change this template use File | Settings | File Templates.
 */

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
}