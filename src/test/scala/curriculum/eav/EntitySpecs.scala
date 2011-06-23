package curriculum.eav

import org.specs.Specification

class EntitySpecs extends Specification {

  "Entity use cases" should {
    "help in design" in {
      import ValueImplicits._

      val civility = Entity("civility",
        ("FirstName", DataType.Text),
        ("LastName", DataType.Text),
        ("DateOfBirth", DataType.Date),
        ("Gender", DataType.Choice("Male", "Female"))
      )
      civility.entityName mustEq "civility"
      civility.getAttributes.size mustEq  4
    }
  }
}