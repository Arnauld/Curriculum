package curriculum.message

import org.specs.Specification

class MessageTypeSpecs extends Specification {

  "Builtin types" should {
    "have a constant code" in {
      Message.Type.Info.code must_== "type-info"
      Message.Type.Warn.code must_== "type-warning"
      Message.Type.Success.code must_== "type-success"
      Message.Type.Error.code must_== "type-error"
      Message.Type.Unknown.code must_== "type-unknown"
    }

    "be used by 'Info' factory method" in {
      val msg = Message.info(Message.code("hello %s!"), 17L)
      msg.messageType must_== Message.Type.Info
      msg.messageCode.rawMessage() must_== "hello %s!"
    }
    "be used by 'Warn' factory method" in {
      val msg = Message.warn(Message.code("hello %s!"), 17L)
      msg.messageType must_== Message.Type.Warn
      msg.messageCode.rawMessage() must_== "hello %s!"
    }
    "be used by 'Error' factory method" in {
      val msg = Message.error(Message.code("hello %s!"), 17L)
      msg.messageType must_== Message.Type.Error
      msg.messageCode.rawMessage() must_== "hello %s!"
    }
    "be used by 'Success' factory method" in {
      val msg = Message.success(Message.code("hello %s!"), 17L)
      msg.messageType must_== Message.Type.Success
      msg.messageCode.rawMessage() must_== "hello %s!"
    }
  }
}