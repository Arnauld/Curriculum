package curriculum.cluster

import java.io.{DataInputStream, DataOutputStream}

trait ClusterJob {
  def actionName:String
  def writeQuery(out:DataOutputStream)
  def readResponse(in:DataInputStream)
  def isDone:Boolean
}





