package curriculum.eav

import org.specs.Specification
import org.joda.time.LocalDate

class EntitySpecs extends Specification {

  import Attribute._

  "Entity use cases" should {
    "help in design" in {
      val gender = Entity("gender", ("value", TextType))
      val civility = Entity("civility",
        ("FirstName", TextType),
        ("LastName", TextType),
        ("DateOfBirth", LocalDateType),
        ("Gender", gender.entityType)
      )
      civility.entityName mustEq "civility"
      civility.getAttributes.size mustEq 4
    }
  }

  "Instance created through Entity" should {
    val genderEntity = Entity("gender", ("value", TextType))
    val civilityEntity = Entity("civility",
      ("FirstName", TextType),
      ("LastName", TextType),
      ("DateOfBirth", LocalDateType),
      ("Gender", genderEntity.entityType)
    )
    "not have any value by default" in {
      val instance = civilityEntity.newInstance
      instance mustVerify (_ != null)
      instance("FirstName") must_== None
      instance("LastName") must_== None
      instance("DateOfBirth") must_== None
      instance("Gender") must_== None
    }

    "allow values to be defined" in {
      val male = genderEntity.newInstance("value", "Male")
      val female = genderEntity.newInstance("value", "Female")

      val instance = civilityEntity.newInstance
      instance("FirstName", "Carmen")
      instance("LastName", "McCallum")
      instance("DateOfBirth", new LocalDate(2011, 1, 19))
      instance("Gender", female)

      instance("FirstName").get must_== "Carmen"
      instance("LastName").get must_== "McCallum"
      instance("DateOfBirth").get must_== new LocalDate(2011, 1, 19)
      instance("Gender").get must_== female
    }

    "convert compatible type values" in {
      skip("ignore")
      val instance = civilityEntity.newInstance
      instance("FirstName", 17)
      instance("FirstName").get must_== "17"
    }

    "not accept invalid values" in {
      val instance = civilityEntity.newInstance
      instance("FirstName", 17)

    }

  }

}