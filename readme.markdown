Curriculum: an EAV based curriculum vitae
==============================================

## Libraries

* [SBT 0.10.0](https://github.com/harrah/xsbt)
* [SBT web plugin](https://github.com/siasia/xsbt-web-plugin)
* [Scalatra](https://github.com/scalatra/scalatra)
* [jackson JSON](http://jackson.codehaus.org/)
* [Netty](http://www.jboss.org/netty)
* [Google Guava](http://code.google.com/p/guava-libraries/)
* [Hadoop Zookeeper](http://zookeeper.apache.org/)


* [Berkeley DB](http://www.oracle.com/technetwork/database/berkeleydb/overview/index.html) for persistence
* [Guice](http://code.google.com/p/google-guice/)

## Patterns

* [Real-World Scala: Dependency Injection (DI)](http://jonasboner.com/2008/10/06/real-world-scala-dependency-injection-di.html)
* [DCI in Real World](http://sadekdrobi.com/2009/06/10/dci-in-real-world-domain-context-and-interaction-with-scala-in-a-real-world-project/)

## Code track


### EAV design

EAV meta models are defined in `curriculum/eav`


* `DataType.scala` contains all the predefined DataType currently supported by the models
* `Entity.scala` define the Entity/Attribute meta models and their relationship. Entity provides method to create corresponding
   new instance.
* `Instance.scala` is the default implementation of an instance, it stores the values of the attributes that are defined
   in the corresponding entity.
* `Persistent.scala` is not yet used, and serves as storage for my random thoughts around persistent behavior.


`curriculum/eav/service` contains the basic layer of service

* `ModelLoader` is an xml loader of entity definitions, see `curriculum/domain/CurriculumVitaeModels` for a sample model.
* `InstanceLoader` is an xml loader of instances, see `curriculum/domain/CurriculumVitaeModels` for sample instances based on the previous models.
* InstanceService`/`EntityService` provides basic functionality around instance and model, such as basic repository

### EAV sample case: Curriculum Vitae

Sample use case is in `curriculum/domain`. It contains both model and instance definitions, and the web page used to
render instance.

### Utilities

'curriculum/util' contains several utilities. The two most interestings are `HasLabel` and `HasHtmlDescription` used
to have a basic support for localization of models.

'curriculum/message' provides base classes for i18n of internal message bus. The messages posted in the 'local' bus 
are periodically fetched by the browser and displayed to the user.

### Web and page rendering

Web support is provided by the use of *scalatra* (entry point `CurriculumFilter`) and XML manipulation for page rendering.

Currently the page rendering is based on raw html (manipulated as xml). This has been done in order to:

* understand how to manipulate XML through scala: read, write, xpath, merge...
* since XML support is builtin in scala. A lot of data navigation issues is detected early at compilation.

### Message and Queue

Message is a localized support the notificatin within the application.
Currently the message queue is a one for all, with in-memory persistance. Plan is to add support for a
standard queue system such as ActiveMQ (RabbitMQ would have been well suitable but it lacks an embedded
support in Java).

Messages are regulary polled within javascript 'cluster.js' and logged. Type of message is used for
icon rendering through css.


### Cluster

Cluster support includes

* Simulation of node start by starting embedded netty server
* Round robin job distribution among started node

Plan is to move the cluster nodes and job queue to zookeeper.

## Roadmap

* Add message display on a per session basis: "i trigger the search, i'm the only one that see its life events"
* Replace job/dispatch by an event bus, e.g. based on ActiveMQ/RabbitMQ
* Add instance rendering based on customized/user defined scaml templates
* Add session auth.
* Add persistence behavior
* Add scafold on instance on a per entity basis
* Add entity designer
* Replace object serialization by Protocol Buffer

Misc
==============================================

logs

        tail -f logs/curriculum.log

jetty

        ~sbt> jetty-run


## Sbt

switch to offline mode

        set offline:=true

JSON
==============================================

unfortunately 'jackson-module-scala' seems to not work as is with scala 2.9.0-1...

https://github.com/FasterXML/jackson-module-scala

        git clone https://github.com/FasterXML/jackson-module-scala.git

in pom.xml edit packaging from 'bundle' to 'jar'
and scala version from '2.8.0' to '2.9.0-1'

        mvn -DaltDeploymentRepository=repo::default::file:../arnauld.github.com/maven2 clean install source:jar javadoc:jar deploy