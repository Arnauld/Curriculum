package curriculum.cluster.zookeeper

import org.apache.zookeeper.ZooKeeper

trait ZookeeperComponent {
  def zookeeper:Option[ZooKeeper]
  def zookeeperOrFail = zookeeper.getOrElse({
      throw new ZookeeperAccessException("No zookeeper instance, make sure to call connect")
    })
}
