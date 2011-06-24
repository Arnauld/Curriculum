package curriculum.eav

import org.specs.Specification
import curriculum.eav.ValueImplicits._
import org.joda.time.LocalDate

class EntitySpecs extends Specification {

  "Entity use cases" should {
    "help in design" in {

      val civility = Entity("civility",
        ("FirstName", DataType.Text),
        ("LastName", DataType.Text),
        ("DateOfBirth", DataType.DayMonthYear),
        ("Gender", DataType.Choice(List("Male", "Female")))
      )
      civility.entityName mustEq "civility"
      civility.getAttributes.size mustEq  4
    }
  }

  "Instance created through Entity" should {
    val civilityEntity = Entity("civility",
      ("FirstName", DataType.Text),
      ("LastName", DataType.Text),
      ("DateOfBirth", DataType.DayMonthYear),
      ("Gender", DataType.Choice(List("Male", "Female")))
    )
    "not have any value by default" in {
      val instance = civilityEntity.newInstance
      instance mustVerify(_ != null)
      instance("FirstName") must_== None
      instance("LastName") must_== None
      instance("DateOfBirth") must_== None
      instance("Gender") must_== None
    }

    "allow values to be defined" in {
      val instance = civilityEntity.newInstance
      instance("FirstName", "Carmen")
      instance("LastName", "McCallum")
      instance("DateOfBirth", (2011, 1, 19))
      instance("Gender", Value.Choice(List("Female")))

      instance("FirstName").get must_== Value.Text("Carmen")
      instance("LastName").get must_== Value.Text("McCallum")
      instance("DateOfBirth").get must_== Value.DayMonthYear(new LocalDate(2011,1,19))
      instance("Gender").get must_== Value.Choice(List("Female"))
    }

    "convert compatible type values" in {
      skip("ignore")
      val instance = civilityEntity.newInstance
      instance("FirstName", 17)
      instance("FirstName").get must_== Value.Text("17")
    }

    "not accept invalid values" in {
      val instance = civilityEntity.newInstance
      instance("FirstName", 17)

    }

  }

}