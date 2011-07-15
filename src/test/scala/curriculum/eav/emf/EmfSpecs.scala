package curriculum.eav.emf

import org.specs.Specification
import org.eclipse.emf.ecore._

class EmfSpecs extends Specification {

  import EMF._
  import scala.collection.JavaConversions._

  "EMF" should {
    "be able to define an eav meta model" in {
      val eavPkg = generateEavPackage
      eavPkg must_!= null
      eavPkg.getName must_== "curriculum.eav"
    }

    "be able to create new entity from the models" in {
      val eavPkg = generateEavPackage
      val factory:EFactory = eavPkg.getEFactoryInstance

      def createAttribute(attributeName: String, dataType: String): EObject = {
        val attr = factory.create(attributeClass)
        attr.eSet(attributeClass.getEStructuralFeature("attributeName"), attributeName)
        attr.eSet(attributeClass.getEStructuralFeature("dataType"), dataType)
        attr
      }

      def declareAttributes(entity: EObject, attributes: EObject*) {
        val javaList = seqAsJavaList(attributes)
        entity.eSet(entityClass.getEStructuralFeature("entityAttributes"), javaList)
      }

      val civility = factory.create(entityClass)
      declareAttributes(civility,
        createAttribute("first_name", "dt:text"),
        createAttribute("last_name", "dt:text"),
        createAttribute("date_of_birth", "dt:date")
      )

      def declareEntity(instance:EObject, entity: EObject) {
        instance.eSet(instanceClass.getEStructuralFeature("entity"), entity)
      }

      def declareAttributeValues(instance:EObject, values:(String,Any)*) {
        val F_NAME = nameValuePairClass.getEStructuralFeature("name")
        val F_VALUE = nameValuePairClass.getEStructuralFeature("value")
        val nameValuePairs = values.map({t =>
          val nvPair = factory.create(nameValuePairClass)
          nvPair.eSet(F_NAME, t._1)
          nvPair.eSet(F_VALUE, t._2)
          nvPair

        })
        val javaList = seqAsJavaList(nameValuePairs)
        instance.eSet(instanceClass.getEStructuralFeature("attributeValues"), javaList)
      }

      val sherlock = factory.create(instanceClass)
      declareEntity(sherlock, civility)
      declareAttributeValues(sherlock,
        ("first_name", "Sherlock"),
        ("last_name", "Holmes"),
        ("date_of_birth", "1854/01/06"))
    }
  }

  var attributeClass:EClass = null
  var entityClass:EClass = null
  var nameValuePairClass:EClass = null
  var instanceClass:EClass = null

  def generateEavPackage = {
    implicit val ecoreFactory = EcoreFactory.eINSTANCE

    val eString = EcorePackage.Literals.ESTRING
    val eAny = EcorePackage.Literals.EJAVA_OBJECT

    attributeClass = createClass("Attribute")
    attributeClass.addAttribute("attributeName", eString)
    attributeClass.addAttribute("dataType", eString)

    entityClass = createClass("Entity")
    entityClass.addAttribute("entityName", eString, isId = true, lowerBound = 1)
    entityClass.addAttribute("entityAttributes", attributeClass, upperBound = ETypedElement.UNBOUNDED_MULTIPLICITY)

    nameValuePairClass = createClass("NameValuePair")
    nameValuePairClass.addAttribute("name", eString)
    nameValuePairClass.addAttribute("value", eAny)

    instanceClass = createClass("Instance")
    instanceClass.addReference("entity", entityClass, lowerBound = 1)
    instanceClass.addReference("attributeValues", nameValuePairClass, upperBound = ETypedElement.UNBOUNDED_MULTIPLICITY, containment = true)

    val eavPkg = ecoreFactory.createEPackage()
    eavPkg.setName("curriculum.eav")
    eavPkg.setNsPrefix("curriculum.eav")
    eavPkg.setNsURI("http://curriculum.eav.ecore")
    eavPkg.getEClassifiers.add(attributeClass)
    eavPkg.getEClassifiers.add(nameValuePairClass)
    eavPkg.getEClassifiers.add(entityClass)
    eavPkg.getEClassifiers.add(instanceClass)
    eavPkg
  }
}

object EMF {

  implicit def enhanceEAttribute(eAttribute: EAttribute): EAttributeEnhancer = new EAttributeEnhancer(eAttribute)

  implicit def enhanceEClass(eClass: EClass): EClassEnhancer = new EClassEnhancer(eClass)


  def createClass(name: String, attributes: EAttribute*)(implicit factory: EcoreFactory): EClass = {
    val clazz = factory.createEClass()
    clazz << (attributes: _*)
    clazz
  }


  def attribute(name: String, dataType: EClassifier)(implicit factory: EcoreFactory): EAttribute = {
    val attr = factory.createEAttribute()
    attr --> (name, dataType)
    attr
  }

  class EClassEnhancer(val eClazz: EClass) {
    def <<(attributes: EAttribute*): EClassEnhancer = {
      attributes.foreach(eClazz.getEStructuralFeatures.add(_))
      this
    }

    def addReference(referenceName: String,
                     kind: EClassifier,
                     lowerBound: Int = 0,
                     upperBound: Int = 1,
                     containment:Boolean = false)(implicit factory: EcoreFactory):EReference = {
      val reference = factory.createEReference
      // always add to container first
      eClazz.getEStructuralFeatures.add(reference);
      reference.setName(referenceName);
      reference.setEType(kind)
      reference.setLowerBound(lowerBound)
      reference.setUpperBound(upperBound)
      reference.setContainment(containment)
      reference
    }

    def addAttribute(attributeName: String,
                     kind: EClassifier,
                     isId: Boolean = false,
                     lowerBound: Int = 0,
                     upperBound: Int = 1)(implicit factory: EcoreFactory):EAttribute = {
      val attribute = factory.createEAttribute
      // always add to container first
      eClazz.getEStructuralFeatures.add(attribute)
      attribute.setName(attributeName)
      attribute.setEType(kind)
      attribute.setID(isId)
      attribute.setLowerBound(lowerBound)
      attribute.setUpperBound(upperBound)
      attribute
    }
  }

  class EAttributeEnhancer(val eAttribute: EAttribute) {
    def -->(t: (String, EClassifier)): EAttributeEnhancer = {
      eAttribute.setName(t._1)
      eAttribute.setEType(t._2)
      this
    }
  }

}