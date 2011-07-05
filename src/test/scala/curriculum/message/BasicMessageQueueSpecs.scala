package curriculum.message

import org.specs.Specification

class BasicMessageQueueSpecs extends Specification {

  val DefaultLimit = 10

  "BasicMessageQueue" should {
    "set message id on publish" in {
      val msg1 = Message.info(Message.code("Hello!"))
      val queue = new BasicMessageQueue ("queue")
      queue.publish(msg1)
      msg1.messageId must_== 1
    }

    "publish one message and list it" in {
      val msg1 = Message.info(Message.code("Hello!"))
      val queue = new BasicMessageQueue ("queue")
      queue.publish(msg1)
      val list = queue.listMessages(0)
      list.size must_== 1
      list(0) must_== msg1
    }

    "publish two messages and list them in order" in {
      val msg1 = Message.info(Message.code("Hello 1!"))
      val msg2 = Message.info(Message.code("Hello 2!"))
      val queue = new BasicMessageQueue ("queue")
      queue.publish(msg1)
      queue.publish(msg2)
      val list = queue.listMessages(0)
      list.size must_== 2
      list(0) must_== msg1
      list(1) must_== msg2
    }

    "publish multiple messages and list them in order" in {
      val msgs = 1.to(15).map({
        i =>
          Message.info(Message.code("Hello " + i + "!"))
      }).toList
      val queue = new BasicMessageQueue ("queue")
      msgs.foreach(queue.publish(_))

      val list = queue.listMessages(0)
      list.size must_== DefaultLimit
      0.until(DefaultLimit).foreach({
        i =>
          list(i).messageId must_== msgs(i).messageId
      })
    }

    "be properly cleaned by a MarkAndSweep invocation" in {
      val msgs = 1.to(15).map({
        i =>
          Message.info(Message.code("Hello " + i + "!"))
      }).toList
      val queue = new BasicMessageQueue ("queue")
      msgs.foreach(queue.publish(_))

      val listBefore = queue.listMessages(0)
      listBefore.size must_== DefaultLimit

      queue.markAndSweep()
      val listAfter = queue.listMessages(0)
      listAfter.size must_== DefaultLimit

    }
  }
}