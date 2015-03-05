# Make and Build Vessl

Welcome to the Make & Build Vessl framework which provides application developers with tools to:
* perform data access in a lightweight manner
* create and execute fixtures for loading data
* perform validation logic with the data services
* integrate with RESTful services
* configuration via an environment name - this will allow for resource matching for a property file included in the classpath
* configuration via a propertyfile - the passed system property to the jvm will be used to load this property file

Make & Build Vessl is licensed under the Apache Public License, Version 2.0 (see [LICENSE](./LICENSE))



## JDBC based persistence


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
* supports domain model specialization inheritance (see the @Specialize annotation and the UserDao_IT.testSpecialized() test)

For an example of the usage see UserDao_IT

## Fixtures

You can also make use of the fixture functionality to load test data from class resources.  the solution this is based upon seperates loaders from persisters and takes into account order of data that is being loaded.  if you have cross referencing associations, then you will probably want to write your own custom EntityLoaders and EntityManagers.

* src/test/resources/fixtures includes json resources to be loaded
* src/test/java/integration/com/makeandbuild/vessl/fixture/Fixture_IT.java has the tests for loading and purging data
* src/test/resources/spring.xml definition of fixture sets up the meta data for the project and takes into account the ordering

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

For a full example, please see the SpringEnvironmentPropertyPlaceholderConfigurerTest for some examples.  You can also see the configuration of src/test/resources/spring-propconfig.xml


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

## Integration tests

Create your database

    mysql -u root
    create database mnb_persistence

Now load the user table

    mysql -u root mnb_persistence < ./src/test/resources/create_user.sql
    mysql -u root mnb_persistence < ./src/test/resources/create_event.sql

Now you can run the function test

     mvn integration-test -Pft