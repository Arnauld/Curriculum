package curriculum.util

import org.slf4j.LoggerFactory
import org.codehaus.jackson.map.ObjectMapper
import java.io._
import org.codehaus.jackson.`type`.TypeReference
import com.fasterxml.jackson.module.scala.ScalaModule
import org.codehaus.jackson.{JsonToken, JsonParser, JsonGenerator, JsonFactory}

trait ToJSON {
  def toJSONString(ctx: Map[Any, Any] = Map.empty[Any, Any]): String = {
    val writer = new StringWriter()
    writeJSON(writer, ctx)
    writer.toString
  }

  def writeJSONWithoutContext(writer: Writer) {
    writeJSON(writer: Writer, Map.empty[Any, Any])
  }

  def writeJSON(writer: Writer, ctx: Map[Any, Any]) {
    val f = new JsonFactory()
    implicit val g = f.createJsonGenerator(writer)
    writeJSON(g, ctx)
    g.close() // important: will force flushing of output, close underlying output stream
  }

  def writeJSONWithoutContext(stream: OutputStream) {
    writeJSON(stream: OutputStream, Map.empty[Any, Any])
  }

  def writeJSON(stream: OutputStream, ctx: Map[Any, Any]) {
    val f = new JsonFactory()
    implicit val g = f.createJsonGenerator(stream)
    writeJSON(g, ctx)
    g.close() // important: will force flushing of output, close underlying output stream
  }

  def writeJSONContent(g: JsonGenerator, ctx: Map[Any, Any])

  def writeJSON(g: JsonGenerator, ctx: Map[Any, Any]) {
    import curriculum.util.ToJSON._
    writeObject({
      g =>
        writeJSONContent(g, ctx)
    })(g)
  }

  def writeJSONField(fieldName: String, g: JsonGenerator, ctx: Map[Any, Any]) {
    import curriculum.util.ToJSON._
    writeObjectField(fieldName, {
      g =>
        writeJSONContent(g, ctx)
    })(g)
  }

}

object ToJSON {
  val log = LoggerFactory.getLogger(classOf[ToJSON])

  private val objectMapper = new ObjectMapper

  private val jsonFactory = new JsonFactory
  var prettyPrint = true

  def toJson(pojo:AnyRef):Array[Byte] = {
    val bout = new ByteArrayOutputStream()
    toJson(pojo, bout)
    bout.toByteArray
  }

  def toJson(pojo:AnyRef, out:OutputStream) {
    val jg = jsonFactory.createJsonGenerator(out)
    if (prettyPrint) {
        jg.useDefaultPrettyPrinter()
    }
    objectMapper.writeValue(jg, pojo)
  }


  def valueFromJson[T](jsonAsString: String, clazz: Class[T]): T = {
    objectMapper.readValue(jsonAsString, clazz);
  }

  def valueFromJson[T](jsonAsBytes: Array[Byte], clazz: Class[T]): T = {
    objectMapper.readValue(jsonAsBytes, clazz);
  }

  def valueFromJson[T](jsonAsBytes: DataInputStream, clazz: Class[T]): T = {
    objectMapper.readValue(jsonAsBytes, clazz);
  }

  def valuesFromJson[T](jsonAsStream: DataInputStream, clazz: Class[T]): List[T] = {
    val parser = jsonFactory.createJsonParser(jsonAsStream)
    parseValues(parser, clazz)
  }

  def valuesFromJson[T](jsonAsString: String, clazz: Class[T]): List[T] = {
    val parser = jsonFactory.createJsonParser(jsonAsString)
    parseValues(parser, clazz)
  }

  def parseValues[T](parser:JsonParser, clazz: Class[T]): List[T] = {
    if (parser.nextToken() != JsonToken.START_ARRAY) {
      throw new MalformedDataException("Illegal state: values must start with a <start_array> token")
    }
    var token = parser.nextToken()
    var elems:List[T] = Nil
    while(token != JsonToken.END_ARRAY) {
      elems = objectMapper.readValue(parser, clazz) :: elems
      token = parser.nextToken()
    }
    elems
  }

  def toJSONString(seq: Iterable[ToJSON], ctx: Map[Any, Any] = Map.empty[Any, Any]): String = {
    val writer = new StringWriter()
    writeArray(writer, seq, ctx)
    writer.toString
  }

  def writeArray(writer: Writer, seq: Iterable[ToJSON], ctx: Map[Any, Any] = Map.empty[Any, Any]) {
    val f = new JsonFactory()
    implicit val g = f.createJsonGenerator(writer)
    if (prettyPrint) {
        g.useDefaultPrettyPrinter();
    }
    writeArray({
      g =>
        seq.foreach(_.writeJSON(g, ctx))
    })
    g.close() // important: will force flushing of output, close underlying output stream
  }

  def writeValue(a: Any, ctx: Map[Any, Any] = Map.empty[Any, Any])(implicit g: JsonGenerator) {
    a match {
      case x: String => g.writeString(x)
      case x: Int => g.writeNumber(x)
      case x: ToJSON => x.writeJSON(g, ctx)
      case m: Map[_, _] =>
        log.debug("Writing value as Map {}", a)
        g.writeStartObject()
        m.foreach({
          e => writeField(e._1.asInstanceOf[String], e._2, ctx)
        })
        g.writeEndObject()
      case x: Long => g.writeNumber(x)
      case x: Float => g.writeNumber(x)
      case x: Double => g.writeNumber(x)
      case a: Iterable[_] =>
        log.debug("Writing value as Array {}", a)
        writeArray({
          g =>
            a.foreach(writeValue(_)(g))
        })(g)
    }
  }

  def writeField(name: String, a: Any, ctx: Map[Any, Any] = Map.empty[Any, Any])(implicit g: JsonGenerator) {
    log.debug("About to write field {}", name)
    a match {
      case x: String => g.writeStringField(name, x)
      case x: Int => g.writeNumberField(name, x)
      case x: ToJSON =>
        log.debug("Writing field as ToJSON {}", name)
        x.writeJSONField(name, g, ctx)
      case m: Map[_, _] =>
        log.debug("Writing field as Map {}", name)
        g.writeObjectFieldStart(name)
        m.foreach({
          e => writeField(e._1.asInstanceOf[String], e._2)
        })
        g.writeEndObject()
      case x: Long => g.writeNumberField(name, x)
      case x: Float => g.writeNumberField(name, x)
      case x: Double => g.writeNumberField(name, x)
      case a: Iterable[_] =>
        log.debug("Writing field as Array {}", name)
        writeArrayField(name, {
          g =>
            a.foreach(writeValue(_)(g))
        })(g)
      case unknown: AnyRef =>
        log.warn("Unable to write field <{}> with value of type <{}>", name, unknown.getClass)
    }
  }

  def writeObject(objectContent: (JsonGenerator) => Any)(implicit g: JsonGenerator) {
    g.writeStartObject()
    objectContent(g)
    g.writeEndObject()
  }

  def writeObjectField(name: String, objectContent: (JsonGenerator) => Any)(implicit g: JsonGenerator) {
    g.writeObjectFieldStart(name)
    objectContent(g)
    g.writeEndObject()
  }


  def writeArrayField(name: String, arrayContent: (JsonGenerator) => Any)(implicit g: JsonGenerator) {
    g.writeArrayFieldStart(name)
    arrayContent(g)
    g.writeEndArray()
  }

  def writeArray(arrayContent: (JsonGenerator) => Any)(implicit g: JsonGenerator) {
    g.writeStartArray()
    arrayContent(g)
    g.writeEndArray()
  }

}