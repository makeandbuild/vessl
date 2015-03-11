# Make and Build Vessl

Welcome to the Make & Build Vessl framework which provides application developers with tools to:
* perform data access in a lightweight manner
* create and execute fixtures for loading data
* perform validation logic with the data services
* expose REST resources to the underlying persistance functionality
* configure applications specific to different environments without requiring seperate packaging

Make & Build Vessl is licensed under the Apache Public License, Version 2.0 (see [LICENSE](./LICENSE))

Please see the sibling project for an example web app https://github.com/makeandbuild/vessl-webapp


## JDBC Based Persistence

This framework is an extension of the spring JDBC implementation providing the following services:
* makes use of JPA annotations in your models - see [User](./src/test/java/integration/com/makeandbuild/vessl/persistence/User.java)
 as an example
* can use the simplified [ReflectionBasedJdbcMapper](./src/main/java/com/makeandbuild/vessl/persistence/jdbc/ReflectionBasedJdbcMapper.java) to easily create your DAOs.  This supports all sorts of types including Date, Integer, Long, Enums (as string column mappings), String - see [UserDaoImpl](./src/test/java/integration/com/makeandbuild/vessl/persistence/UserDaoImpl.java) as an example
* if you dont want the overhead of Reflection you can create those implementations as well by extending [BaseDaoImpl](./src/main/java/com/makeandbuild/vessl/persistence/jdbc/BaseDaoImpl.java)

The BaseDao has a lot of built in functionality including
* criteria based finders
* paging
* sorting
* find by id
* exists finders
* delete helpers
* supports domain model specialization inheritance  - see the [@Specialize](./src/main/java/com/makeandbuild/vessl/persistence/jdbc/Specialize.java) annotation implmented in [User](https://github.com/makeandbuild/vessl/blob/master/src/test/java/integration/com/makeandbuild/vessl/persistence/User.java) and tested in [UserDao_IT.testSpecialized()](./src/test/java/integration/com/makeandbuild/vessl/persistence/UserDao_IT.java)
* join logic for advanced criteria support in [BaseDaoImpl.addQueryJoinSupport()](./src/main/java/com/makeandbuild/vessl/persistence/jdbc/BaseDaoImpl.java) which you call explicity in the constructor of your specialized Daos see see [EventDaoImpl](./src/test/java/integration/com/makeandbuild/vessl/persistence/EventDaoImpl.java)
* cascade deletes for dao based dependencies - simply by annotating your Daos with [@CascadeDelete](./src/main/java/com/makeandbuild/vessl/persistence/jdbc/CascadeDelete.java) see [UserDaoImpl](./src/test/java/integration/com/makeandbuild/vessl/persistence/UserDaoImpl.java)

## Fixtures

You can also make use of the fixture functionality to load test data from class resources.  The solution seperates two seperate conceptual elements:
* [EntityLoader](./src/main/java/com/makeandbuild/vessl/fixture/EntityLoader.java) - is responsible for loading the fixture data from a source
* [EntityManager](./src/main/java/com/makeandbuild/vessl/fixture/EntityManager.java) - is responsible for persisting the loaded entity to the target data source

To understand how to use this, please see
* [src/test/resources/fixtures](./src/test/resources/fixtures) includes json resources to be loaded
* [src/test/resources/spring.xml](src/test/resources/spring.xml) definition of fixture sets up the meta data for the project and takes into account the ordering
* [Fixture_IT.testAll()](src/test/java/integration/com/makeandbuild/vessl/fixture/Fixture_IT.java) demonstrates how you can setup a set of resource files to be loaded in spring and purge() or load() as a complete set
* [Fixture_IT.testResourceSingularly()](src/test/java/integration/com/makeandbuild/vessl/fixture/Fixture_IT.java) demonstrates on how to pass in a resource location to load a resoruce explicity
* [Fixture_IT.testEntityClassSingularly()](src/test/java/integration/com/makeandbuild/vessl/fixture/Fixture_IT.java)  demonstrates how to purge via a given Model class


## Validation

Validation is supported via JSR-303 annotations and custom spring Validator instances you define. Simply defining an
instance of [ValidationProxyManagerImpl](./src/main/java/com/makeandbuild/vessl/validation/ValidationProxyManagerImpl.java) in the application context will suffice to load (and cache) all custom
Validators you have also defined in the application context. Validation does not occur by default on any of your Dao
classes.

To enable bean validation you should create a dynamic proxy instance of your Dao (make sure it extends [BaseDao](./src/main/java/com/makeandbuild/vessl/persistence/jdbc/BaseDao.java)) and perform your methods through that proxy. All calls made through your dao proxy instance will have its
parameters ran through a Validator instance that supports the parameters object type.

Multiple validators can be ran
against the same object. This is controlled by the supports(...) method of your custom Validator instance. If a
validation error is encountered the proxy dao instance will throw a RuntimeException ([BeanValidationException](./src/main/java/com/makeandbuild/vessl/validation/exception/BeanValidationException.java)) that
contains a list of ObjectError objects defining the validation errors that occured.

## Property Configuration

It's possible to configure your application via an environment name which will match to a resource property file included in the classpath.  This is great if you dont mind including environment settings in your packaging.  For a full example, please see the [SpringEnvironmentPropertyPlaceholderConfigurerTest](./src/test/java/unit/com/makeandbuild/vessl/propconfig/SpringEnvironmentPropertyPlaceholderConfigurerTest.java).  You can also see the configuration of [src/test/resources/spring-propconfig.xml](./src/test/resources/spring-propconfig.xml).  Here is a snippet in $TOMCAT_HOME/bin/setenv.sh using the environment name (will load [/config-dev.properties](./src/test/resources/config-dev.properties) in classpath)

    JAVA_OPTS="-DenvironmentName=dev -Dlog4j.configuration=file:/home/dev/log4j.properties"
    export JAVA_OPTS

It's also possible to explicitly define the property file on the local filesystem you want to use.  If you are in dev/ops this is probably what you want to have for your produciton environmets.  Here is a snippet in $TOMCAT_HOME/bin/setenv.sh using the full filename

    JAVA_OPTS="-DenvironmentFilename=/home/dev/config-dev.properties -Dlog4j.configuration=file:/home/dev/log4j.properties"
    export JAVA_OPTS

# REST resources

If you look at [EventResource](https://github.com/makeandbuild/vessl-webapp/blob/master/src/main/java/com/makeandbuild/vessl/sample/rest/EventResource.java) you'll see that there is a lot built into a class including:
* GET {resource}/#id
* GET {resource}
* PUT {resource}/#id
* POST {resource}

NOTE that POST and PUT expect shallow objects.  GET methods can support rendering nested/full objects, if the developer implements the serializers in the REST resource - see [EventResource](https://github.com/makeandbuild/vessl-webapp/blob/master/src/main/java/com/makeandbuild/vessl/sample/rest/EventResource.java) and [EventSerializer](https://github.com/makeandbuild/vessl-webapp/blob/master/src/main/java/com/makeandbuild/vessl/sample/rest/serializers/EventSerializer.java)

    GET http://localhost:8080/vessl-webapp/rest/events
    RESPONSE
    {
      "items": [
        {
          "id": "100-1",
          "parent": {
            "id": "1231231231-222",
            "type": "user.loggedout"
          },
          "type": "child.user.loggedout"
        },
        {
          "id": "100-2",
          "parent": {
            "id": "1231231231-12312312-12-3123123",
            "type": "user.loggedin"
          },
          "type": "child.user.loggedin"
        },
        {
          "id": "1231231231-12312312-12-3123123",
          "type": "user.loggedin"
        },
        {
          "id": "1231231231-222",
          "type": "user.loggedout"
        }
      ],
      "totalPages": 1,
      "totalItems": 4
    }

Resource validation logic for the persistence layer - see [EventValidator](https://github.com/makeandbuild/vessl-webapp/blob/master/src/main/java/com/makeandbuild/vessl/sample/validators/EventValidator.java) and [UserValidatior](https://github.com/makeandbuild/vessl-webapp/blob/master/src/main/java/com/makeandbuild/vessl/sample/validators/UserValidator.java)

    POST http://localhost:8080/vessl-webapp/rest/users
    {
      "id": 9999,
      "username": "azuercher",
      "loginCount": 1,
      "createdAt": 1426031160872,
      "userType": "simple",
      "longitude": -84.436287,
      "latitude": 33.801078
    }
    RESPONSE
    {
      "errors": [
        {
          "codes": [
            "local.error.exists.com.makeandbuild.vessl.sample.domain.User.username",
            "local.error.exists.username",
            "local.error.exists.java.lang.String",
            "local.error.exists"
          ],
          "defaultMessage": "Username already taken",
          "objectName": "com.makeandbuild.vessl.sample.domain.User",
          "field": "username",
          "rejectedValue": "azuercher",
          "bindingFailure": false,
          "code": "local.error.exists"
        }
      ],
      "localizedMessage": "com.makeandbuild.vessl.sample.domain.User validation failed",
      "message": "com.makeandbuild.vessl.sample.domain.User validation failed",
      "validatedBean": {
        "id": 9999,
        "createdAt": "2015-03-10T23:46:00.872+0000",
        "latitude": 33.801078,
        "loginCount": 1,
        "longitude": -84.436287,
        "username": "azuercher",
        "userType": "simple"
      }
    }

Paging is built into the framework for the list functionality where page index starts at 0

    GET http://localhost:8080/vessl-webapp/rest/events?pageSize=2&page=0
    RESPONSE
    {
      "items": [
        {
          "id": "100-1",
          "parent": {
            "id": "1231231231-222",
            "type": "user.loggedout"
          },
          "type": "child.user.loggedout"
        },
        {
          "id": "100-2",
          "parent": {
            "id": "1231231231-12312312-12-3123123",
            "type": "user.loggedin"
          },
          "type": "child.user.loggedin"
        }
      ],
      "totalPages": 3,
      "totalItems": 6
    }

    GET http://localhost:8080/vessl-webapp/rest/events?pageSize=2&page=1
    RESPONSE
    {
      "items": [
        {
          "id": "1231231231-12312312-12-3123123",
          "type": "user.loggedin"
        },
        {
          "id": "1231231231-222",
          "type": "user.loggedout"
        }
      ],
      "totalPages": 3,
      "totalItems": 6
    }

Query support cascades into the course grained persistence layer for the list functionality querying with a $attributeName=$value notation

    GET http://localhost:8080/vessl-webapp/rest/events?type=user.loggedin
    RESPONSE
    {
      "items": [
        {
          "id": "1231231231-12312312-12-3123123",
          "type": "user.loggedin"
        }
      ],
      "totalPages": 1,
      "totalItems": 1
    }

Like and other operators besides equals are also supported.  here we look for all events that have a type that starts with "user."

    GET http://localhost:8080/vessl-webapp/rest/events?type=user.%&typeOperation=like
    RESPONSE
    {
      "items": [
        {
          "id": "1231231231-12312312-12-3123123",
          "type": "user.loggedin"
        },
        {
          "id": "1231231231-222",
          "type": "user.loggedout"
        },
        {
          "id": "1231231231-223",
          "type": "user.loggedout"
        },
        {
          "id": "1231231231-224",
          "type": "user.loggedout"
        }
      ],
      "totalPages": 1,
      "totalItems": 4
    }
Using joined properties in the criteria is also supported for join attributes defined via [BaseDaoImpl.addQueryJoinSupport()](./src/main/java/com/makeandbuild/vessl/persistence/jdbc/BaseDaoImpl.java) and implemented in [EventDaoImpl](https://github.com/makeandbuild/vessl-webapp/blob/master/src/main/java/com/makeandbuild/vessl/sample/persistence/EventDaoImpl.java), here we look based upon the Event attribute "user.username"

    GET http://localhost:8080/vessl-webapp/rest/events?user.username=telrod
    RESPONSE
    {
      "items": [
        {
          "id": "1231231231-223",
          "user": {
            "id": 2,
            "createdAt": "1988-01-01T00:00:00.000+0000",
            "latitude": 33.801078,
            "loginCount": 1,
            "longitude": -84.436287,
            "username": "telrod",
            "userType": "simple"
          },
          "type": "user.loggedout"
        },
        {
          "id": "1231231231-224",
          "user": {
            "id": 2,
            "createdAt": "1988-01-01T00:00:00.000+0000",
            "latitude": 33.801078,
            "loginCount": 1,
            "longitude": -84.436287,
            "username": "telrod",
            "userType": "simple"
          },
          "type": "user.loggedout"
        }
      ],
      "totalPages": 1,
      "totalItems": 2
    }

    GET http://localhost:8080/vessl-webapp/rest/events?user.username=azuercher
    RESPONSE
    {
      "items": [
        {
          "id": "1231231231-222",
          "user": {
            "id": 1,
            "createdAt": "2012-01-01T00:00:00.000+0000",
            "latitude": 33.801078,
            "loginCount": 1,
            "longitude": -84.436287,
            "username": "azuercher",
            "userType": "admin"
          },
          "type": "user.loggedout"
        }
      ],
      "totalPages": 1,
      "totalItems": 1
    }

Sorting is also supported - here we sort by "type" ascending

    GET http://localhost:8080/vessl-webapp/rest/events?sortBys=type:true
    RESPONSE
    {
      "items": [
        {
          "id": "100-2",
          "parent": {
            "id": "1231231231-12312312-12-3123123",
            "type": "user.loggedin"
          },
          "type": "child.user.loggedin"
        },
        {
          "id": "100-1",
          "parent": {
            "id": "1231231231-222",
            "type": "user.loggedout"
          },
          "type": "child.user.loggedout"
        },
        {
          "id": "1231231231-12312312-12-3123123",
          "type": "user.loggedin"
        },
        {
          "id": "1231231231-222",
          "type": "user.loggedout"
        },
        {
          "id": "1231231231-223",
          "type": "user.loggedout"
        },
        {
          "id": "1231231231-224",
          "type": "user.loggedout"
        }
      ],
      "totalPages": 1,
      "totalItems": 6
    }

As well as descending sorting and multple attributes - here we sort by "type" ascending and then by "id" descending

    GET http://localhost:8080/vessl-webapp/rest/events?sortBys=type:true,id:false
    RESPONSE
    {
      "items": [
        {
          "id": "100-2",
          "parent": {
            "id": "1231231231-12312312-12-3123123",
            "type": "user.loggedin"
          },
          "type": "child.user.loggedin"
        },
        {
          "id": "100-1",
          "parent": {
            "id": "1231231231-222",
            "type": "user.loggedout"
          },
          "type": "child.user.loggedout"
        },
        {
          "id": "1231231231-12312312-12-3123123",
          "type": "user.loggedin"
        },
        {
          "id": "1231231231-224",
          "type": "user.loggedout"
        },
        {
          "id": "1231231231-223",
          "type": "user.loggedout"
        },
        {
          "id": "1231231231-222",
          "type": "user.loggedout"
        }
      ],
      "totalPages": 1,
      "totalItems": 6
    }

## Integration Tests

Create your database

    mysql -u root
    create database vessl

Now create the user and event tables

    mysql -u root vessl < ./src/test/resources/create_user.sql
    mysql -u root vessl < ./src/test/resources/create_event.sql

Now you can run the function test

     mvn integration-test -Pft

For a bigger example of please see the sibling project for an example web app https://github.com/makeandbuild/vessl-webapp
