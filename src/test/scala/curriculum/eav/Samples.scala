package curriculum.eav


object Samples {

  import curriculum.eav.Attribute._

  val civility = new Entity("civility")
  civility.declare("first_name" -> TextType)
  civility.declare("last_name" -> TextType)

  val person = new Entity("person")
  person.declare(new Attribute("civility", EntityType("civility")))
  person.declare(new Attribute("hobbies", TextType, Some(List.empty[String]), -1))

  val sherlock_civility = civility.newInstance(("first_name" -> "Sherlock"), ("last_name" -> "Holmes"))
  val sherlock = person.newInstance(("civility" -> sherlock_civility), ("hobbies" -> List("Anatomy", "Chemistry", "Violin", "Sensational Literature")))

}