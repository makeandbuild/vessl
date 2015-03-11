# Make and Build Vessl

Welcome to the Make & Build Vessl framework which provides application developers with tools to:
* perform data access in a lightweight manner
* create and execute fixtures for loading data
* perform validation logic with the data services
* integrate with RESTful services
* configuration via an environment name - this will allow for resource matching for a property file included in the classpath
* configuration via a propertyfile - the passed system property to the jvm will be used to load this property file

Make & Build Vessl is licensed under the Apache Public License, Version 2.0 (see [LICENSE](./LICENSE))

Please see the sibling project for an example web app https://github.com/makeandbuild/vessl-webapp


## JDBC Based Persistence


This framework is an extension of the spring jdbc implementation.  it provided the following services:
* makes use of jpa annotations in your model class.  See User.java as an example
* can use the simplified ReflectionBasedJdbcMapper to easily create your DAOs.  This supports all sorts of types including Date, Integer, Long, Enums (as string column mappings), String.  See UserDaoImpl as an example
* if you dont want the overhead of Reflection you can create those implementations as well

The BaseDao has a lot of built in methods including
* criteria based finders
* paging
* sorting
* find by id
* exists finders
* delete helpers
* supports domain model specialization inheritance (see the @Specialize annotation and the [UserDao_IT.testSpecialized()](./com/makeandbuild/vessl/persistence/UserDao_IT.java) test)

For an example of the usage see [UserDao_IT](./com/makeandbuild/vessl/persistence/UserDao_IT.java)

## Fixtures

You can also make use of the fixture functionality to load test data from class resources.  the solution this is based upon seperates loaders from persisters and takes into account order of data that is being loaded.  if you have cross referencing associations, then you will probably want to write your own custom EntityLoaders and EntityManagers.

* [src/test/resources/fixtures](./src/test/resources/fixtures) includes json resources to be loaded
* [src/test/java/integration/com/makeandbuild/vessl/fixture/Fixture_IT](src/test/java/integration/com/makeandbuild/vessl/fixture/Fixture_IT.java) has the tests for loading and purging data
* [src/test/resources/spring.xml](src/test/resources/spring.xml) definition of fixture sets up the meta data for the project and takes into account the ordering

There are some tests
* testAll() demonstrates how you can setup a set of resource files to be loaded in spring and purge() or load() as a complete set
* testResourceSingularly() demonstrates on how to pass in a resource location to load a resoruce explicity
* testEntityClassSingularly() demonstrates how to purge via a given Model class


## Validation

Validation is supported via JSR-303 annotations and custom spring Validator instances you define. Simply defining an
instance of ValidationProxyManagerImpl in the application context will suffice to load (and cache) all custom
Validators you have also defined in the application context. Validation does not occur by default on any of your Dao
classes. To enable bean validation you should create a dynamic proxy instance of your Dao (make sure it extends
BaseDao) and perform your methods through that proxy. All calls made through your dao proxy instance will have its
parameters ran through a Validator instance that supports the parameters object type. Multiple validators can be ran
against the same object. This is controlled by the supports(...) method of your custom Validator instance. If a
validation errors is encountered the proxy dao instance will throw a RuntimeException (BeanValidationException) that
contains a list of ObjectError objects defining the validation errors that occured.

## Property Configuration

For a full example, please see the [SpringEnvironmentPropertyPlaceholderConfigurerTest](./src/test/java/unit/com/makeandbuild/vessl/propconfig/SpringEnvironmentPropertyPlaceholderConfigurerTest.java) for some examples.  You can also see the configuration of [src/test/resources/spring-propconfig.xml](./src/test/resources/spring-propconfig.xml)


Here is a snippet in $TOMCAT_HOME/bin/setenv.sh using the full filename

```
JAVA_OPTS="-DenvironmentFilename=/home/dev/config-dev.properties -Dlog4j.configuration=file:/home/dev/log4j.properties"
export JAVA_OPTS
```

Here is a snippet in $TOMCAT_HOME/bin/setenv.sh using the environment name (will load /config-dev.properties in classpath)

```
JAVA_OPTS="-DenvironmentName=dev -Dlog4j.configuration=file:/home/dev/log4j.properties"
export JAVA_OPTS
```

# REST resources

If you look at EventResource you'll see that there is a lot built into a class including:
* show: GET {resource}/#id
* list: GET {resource}
* update: PUT {resource}/#id
* create: POST {resource}

Examples of using serializers to return full objects at render time (see EventResoruce and EventSerializer in https://github.com/makeandbuild/vessl-webapp):

    GET http://localhost:8080/vessl-webapp/rest/events
    {"items":[{"id":"100-1","parent":{"id":"1231231231-222","type":"user.loggedout"},"type":"child.user.loggedout"},{"id":"100-2","parent":{"id":"1231231231-12312312-12-3123123","type":"user.loggedin"},"type":"child.user.loggedin"},{"id":"1231231231-12312312-12-3123123","type":"user.loggedin"},{"id":"1231231231-222","type":"user.loggedout"}],"totalPages":1,"totalItems":4}


Resource validation logic for the persistence layer (see EventValidator and UserValidatior in https://github.com/makeandbuild/vessl-webapp)

    POST http://localhost:8080/vessl-webapp/rest/users {"id":9999,"username":"azuercher","loginCount":1,"createdAt":1426031160872,"userType":"simple","longitude":-84.436287,"latitude":33.801078}
    {"errors":[{"codes":["local.error.exists.com.makeandbuild.vessl.sample.domain.User.username","local.error.exists.username","local.error.exists.java.lang.String","local.error.exists"],"defaultMessage":"Username already taken","objectName":"com.makeandbuild.vessl.sample.domain.User","field":"username","rejectedValue":"azuercher","bindingFailure":false,"code":"local.error.exists"}],"localizedMessage":"com.makeandbuild.vessl.sample.domain.User validation failed","message":"com.makeandbuild.vessl.sample.domain.User validation failed","validatedBean":{"id":9999,"createdAt":"2015-03-10T23:46:00.872+0000","latitude":33.801078,"loginCount":1,"longitude":-84.436287,"username":"azuercher","userType":"simple"}}


Paging is built into the framework for the list functionality (page index starts at 0)

    GET http://localhost:8080/vessl-webapp/rest/events?pageSize=2&page=0
    {"items":[{"id":"100-1","parent":{"id":"1231231231-222","type":"user.loggedout"},"type":"child.user.loggedout"},{"id":"100-2","parent":{"id":"1231231231-12312312-12-3123123","type":"user.loggedin"},"type":"child.user.loggedin"}],"totalPages":3,"totalItems":6}

    GET http://localhost:8080/vessl-webapp/rest/events?pageSize=2&page=1
    {"items":[{"id":"1231231231-12312312-12-3123123","type":"user.loggedin"},{"id":"1231231231-222","type":"user.loggedout"}],"totalPages":3,"totalItems":6}

Query support cascades into the course grained persistence (DAO) layer for the list functionality. this uses a $name=$value notation, but like and other operators besides equals are also supported

    GET http://localhost:8080/vessl-webapp/rest/events?type=user.loggedin
    {"items":[{"id":"1231231231-12312312-12-3123123","type":"user.loggedin"}],"totalPages":1,"totalItems":1}

Sorting is also supported (with multiple attributes)

    GET http://localhost:8080/vessl-webapp/rest/events?sortBys=type:true
    {"items":[{"id":"100-2","parent":{"id":"1231231231-12312312-12-3123123","type":"user.loggedin"},"type":"child.user.loggedin"},{"id":"100-1","parent":{"id":"1231231231-222","type":"user.loggedout"},"type":"child.user.loggedout"},{"id":"1231231231-12312312-12-3123123","type":"user.loggedin"},{"id":"1231231231-222","type":"user.loggedout"},{"id":"1231231231-223","type":"user.loggedout"},{"id":"1231231231-224","type":"user.loggedout"}],"totalPages":1,"totalItems":6}

As well as descending sorting and multple attributes

    GET http://localhost:8080/vessl-webapp/rest/events?sortBys=type:true,id:false
    {"items":[{"id":"100-2","parent":{"id":"1231231231-12312312-12-3123123","type":"user.loggedin"},"type":"child.user.loggedin"},{"id":"100-1","parent":{"id":"1231231231-222","type":"user.loggedout"},"type":"child.user.loggedout"},{"id":"1231231231-12312312-12-3123123","type":"user.loggedin"},{"id":"1231231231-224","type":"user.loggedout"},{"id":"1231231231-223","type":"user.loggedout"},{"id":"1231231231-222","type":"user.loggedout"}],"totalPages":1,"totalItems":6}


## Integration Tests

Create your database

    mysql -u root
    create database vessl

Now create the user and event tables

    mysql -u root vessl < ./src/test/resources/create_user.sql
    mysql -u root vessl < ./src/test/resources/create_event.sql

Now you can run the function test

     mvn integration-test -Pft