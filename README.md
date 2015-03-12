# Make and Build vessl

Welcome to the Make & Build vessl framework which provides application developers with tools to:
* perform data access in a lightweight manner
* create and execute fixtures for loading data
* perform validation logic with the data services
* expose REST resources to the underlying persistance functionality
* configure applications specific to different environments without requiring seperate packaging

Make & Build vessl is licensed under the Apache Public License, Version 2.0 (see [LICENSE](./LICENSE))

Please see the sibling [vessl-webapp](https://github.com/makeandbuild/vessl-webapp) project for an example web app

## Why we created vessl

About 3 years ago we were working on a not entirely small java project that was using hibernate for persistence.  We found that the object maps and the services we built to serialize them had some unintended consequences - even with lazy loading, the object graphs were pulling un-necessarily large amounts of data in the complex objects.  The service contract locked directly into the model and this meant we had to carry excessively large amounts of data around just for some simple update/creates as well as full object graph rendering.  We liked aspects of the spring JDBC framework - it was lightweight, had the concept of domain mappers, and also allowed for direct execution of prepared statements in a secure way so we went with that, unifying the implementation in a course grain way that would allow us to extend it into some other areas, namely:
* fixtures
* REST endpoints
* validation logic

That problem faced another issue as well - how to build a set of assets to be used for deployment and still be able to have environmentally specific configurations.  Our packaging was split across a war file and a set of subproject jar files with resources in them.  We found that the propery configuration approach included in vessl solved this in a elegant and very reusable way.  Its a nice compliment to the items identified above.

This project is the accumlation of several different aspects that we enjoyed from other technolocies/frameworks and have pulled into here to help make application development easier.  We'd really like you to use it, the documentation below explains a bit of that, but our recommendation to see an example of this to pull into or start a new project quickly is to take a look at the [vess-webapp](https://github.com/makeandbuild/vessl-webapp) example project as a consumer of the vessl project would.

## JDBC Based Persistence

This framework is an extension of the spring JDBC implementation providing the following baseline services
* criteria based finders, find by ID, and joined criteria as well
* paging
* sorting
* exists finders
* delete helpers
* specialization for domain model inheritance
* cascade deletes

These are the minimal things you'll have to do:
* define your model class - [User](https://github.com/makeandbuild/vessl/blob/master/src/test/java/integration/com/makeandbuild/vessl/persistence/User.java)
* define your dao interface - [UserDao](https://github.com/makeandbuild/vessl/blob/master/src/test/java/integration/com/makeandbuild/vessl/persistence/UserDao.java)
* define your dao implementations - [UserDaoImpl](https://github.com/makeandbuild/vessl/blob/master/src/test/java/integration/com/makeandbuild/vessl/persistence/UserDaoImpl.java)
* define your dao bean in your [spring.xml](./src/test/resources/spring.xml) along with your dataSource and txManager

Additionally:
* makes use of JPA annotations in your models - see [User](./src/test/java/integration/com/makeandbuild/vessl/persistence/User.java)
 as an example
* can use the simplified [ReflectionBasedJdbcMapper](./src/main/java/com/makeandbuild/vessl/persistence/jdbc/ReflectionBasedJdbcMapper.java) to easily create your DAOs.  This supports all sorts of types including Date, Integer, Long, Enums (as string column mappings), String - see [UserDaoImpl](./src/test/java/integration/com/makeandbuild/vessl/persistence/UserDaoImpl.java) as an example
* if you dont want the overhead of Reflection you can create those implementations as well by extending [BaseDaoImpl](./src/main/java/com/makeandbuild/vessl/persistence/jdbc/BaseDaoImpl.java) but this is a minimal performance tradeoff and will save you a bit of coding by using the [ReflectionBasedJdbcMapper](./src/main/java/com/makeandbuild/vessl/persistence/jdbc/ReflectionBasedJdbcMapper.java)
* specialization - domain model inheritance, see the [@Specialize](./src/main/java/com/makeandbuild/vessl/persistence/jdbc/Specialize.java) annotation implmented in [User](https://github.com/makeandbuild/vessl/blob/master/src/test/java/integration/com/makeandbuild/vessl/persistence/User.java) and tested in [UserDao_IT.testSpecialized()](./src/test/java/integration/com/makeandbuild/vessl/persistence/UserDao_IT.java)
* joined criteria - join logic for advanced criteria support in [BaseDaoImpl.addQueryJoinSupport()](./src/main/java/com/makeandbuild/vessl/persistence/jdbc/BaseDaoImpl.java) which you call explicity in the constructor of your specialized Daos see see [EventDaoImpl](./src/test/java/integration/com/makeandbuild/vessl/persistence/EventDaoImpl.java)
* cascade deletes - for dao based dependencies simply by annotating your Daos with [@CascadeDelete](./src/main/java/com/makeandbuild/vessl/persistence/jdbc/CascadeDelete.java) see [UserDaoImpl](./src/test/java/integration/com/makeandbuild/vessl/persistence/UserDaoImpl.java)

## Couch Based Persistence

As we evolved the JDBC side of our persistence implementation, we also found that the couch side could also be implemented in an abstract way that could make use of many of the things that the JDBC implementation did, in particular:
* criteria based finders
* paging
* find by id
* exists finders
* delete helpers

The contract for this is very course grained and maps to jackson ArrayNode and ObjectNode data types - the overhead to support a fine grained interface goes a bit against the NOSQL approach anyway.  The steps are a bit simpler for Couch than for JDBC because we've moved to a course grained model class:
* you don't have to define a Dao interface unless you want to augment the dao services
* you don't need to define a model class
* you don't need to define domain mappers
* you do need to define a spring bean that extends [CouchDbJacksonImpl](./src/main/java/com/makeandbuild/vessl/persistence/couch/CouchDbJacksonImpl.java) in your [spring.xml](./src/test/resources/spring.xml) like carDao

Here is the bean definition for carDao

    <bean class="com.makeandbuild.vessl.persistence.couch.CouchDbJacksonImpl"
            id="carDao" init-method="init" scope="singleton">
        <property name="template" ref="restTemplate"/>
        <property name="baseUrl" value="http://127.0.0.1:5984"/>
        <property name="databaseName" value="cars"/>
        <property name="designDocument" value="classpath:_design/car.json"/>
        <property name="designDocumentLocation" value="cars/_design/car"/>
    </bean>

[FixtureUtil.testCouchDao()](./src/test/java/integration/com/makeandbuild/vessl/fixture/Fixture_IT.java) has a good example of usage

## Fixtures

You can also make use of the fixture functionality to load test data from class resources.  The solution seperates two seperate conceptual elements:
* [EntityLoader](./src/main/java/com/makeandbuild/vessl/fixture/EntityLoader.java) - is responsible for loading the fixture data from a source
* [EntityManager](./src/main/java/com/makeandbuild/vessl/fixture/EntityManager.java) - is responsible for persisting the loaded entity to the target data source

To get a simple fixture working create your model JSON file like [com.makeandbuild.vessl.persistence.User.json](./src/test/resources/fixtures/com.makeandbuild.vessl.persistence.User.json)

    [
        {
            "createdAt": "2012-01-01T00:00:00.000+0000",
            "id": 1,
            "latitude": 33.801077999999997,
            "loginCount": 1,
            "longitude": -84.436286999999993,
            "userType": "admin",
            "username": "azuercher"
        },
        {
            "createdAt": "1988-01-01T00:00:00.000+0000",
            "id": 2,
            "latitude": 33.801077999999997,
            "loginCount": 1,
            "longitude": -84.436286999999993,
            "userType": "simple",
            "username": "telrod"
        }
    ]

Define your fixture bean in [spring.xml](src/test/resources/spring.xml).  Its important to order your entityManager from most dependent to least dependent.  Conversly order your entityLoaders from least dependent to most dependent.

    <bean class="com.makeandbuild.vessl.fixture.FixtureImpl" id="fixture" scope="singleton">
        <property name="entityLoaders">
            <list>
                <bean class="com.makeandbuild.vessl.fixture.ResourceEntityLoaderImpl">
                    <constructor-arg value="/fixtures/com.makeandbuild.vessl.persistence.User.json"/>
                </bean>
                <bean class="com.makeandbuild.vessl.fixture.ResourceEntityLoaderImpl">
                    <constructor-arg value="/fixtures/com.makeandbuild.vessl.persistence.Event.json"/>
                </bean>
                <bean class="com.makeandbuild.vessl.fixture.ResourceEntityLoaderImpl">
                    <constructor-arg value="/fixtures/com.fasterxml.jackson.databind.node.ObjectNode-car.json"/>
                </bean>
            </list>
        </property>
        <property name="entityManagers">
            <list>
                <bean class="com.makeandbuild.vessl.fixture.DaoEntityManagerImpl">
                    <constructor-arg ref="eventDao"/>
                </bean>
                <bean class="com.makeandbuild.vessl.fixture.DaoEntityManagerImpl">
                    <constructor-arg ref="userDao"/>
                </bean>
                <bean class="com.makeandbuild.vessl.fixture.DaoEntityManagerImpl">
                    <constructor-arg ref="carDao"/>
                    <constructor-arg value="car"/>
                </bean>
            </list>
        </property>
    </bean>

Fixtures have been implemented to support full set loading of the fixture data into memory which is great for small data sets.  This however, becomes an issue when you want to load extremely large data sets which we refer to as "Mega Fixures".  To support this, we wanted to create a iterated loader that allows you to consume a stream and work with an active entity to persist it atomically.  The entityManager's really already support this, so doing so just required us to modify the loader implementation.  If you want to use megaFixtures, make sure that your entityLoader is defined as an instance of [IteratedInputStreamEntityLoaderImpl](src/main/java/com.makeandbuild.vessl.fixture.IteratedInputStreamEntityLoaderImpl)

    <bean class="com.makeandbuild.vessl.fixture.IteratedInputStreamEntityLoaderImpl">
        <property name="inputStream" value="classpath:fixturesgen/com.makeandbuild.vessl.persistence.User.json"/>
        <property name="entityClass" value="com.makeandbuild.vessl.persistence.User"/>
    </bean>

The mega fixtures are defined in [resources/fixturesgen/com.makeandbuild.vessl.persistence.User.json](./src/test/resources/fixturesgen/com.makeandbuild.vessl.persistence.User.json).  To regenerate them again:

    cd src/fixturesgen
    npm install
    ./load.sh

[vess-webapp](https://github.com/makeandbuild/vessl-webapp)  has some nice utility gradle tasks to support mega fixtures and [Fixture_IT](./src/test/java/integration/com/makeandbuild/vessl/fixture/Fixture_IT.java) provides some local coverage for both mega and standard fixtures

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

Here is a sample in [UserValidator](src/test/java//integration/com/makeandbuild/vessl/validation/validators/UserValidator.java)

    public class UserValidator implements Validator {
        @Override
        public boolean supports(Class<?> aClass) {
            return User.class.equals(aClass);
        }
        @Override
        public void validate(Object o, Errors errors) {
            User user = (User) o;
            DateTime dt = new DateTime(user.getCreatedAt().getTime());
            if (dt.year().get() <= 1900) {
                errors.rejectValue("createdAt", "local.error.dateold",
                    "User must have been created after 1900");
            }
        }
    }


# REST resources

There is a lot built into a [ResourceSerializedBase](./src/main/java/com/makeandbuild/vessl/rest/ResourceSerializedBase.java) class including:
* GET {resource}/#id
* GET {resource}
* PUT {resource}/#id
* POST {resource}

Here is an example in [EventResource](https://github.com/makeandbuild/vessl-webapp/blob/master/src/main/java/com/makeandbuild/vessl/sample/rest/EventResource.java)

    @Path("/events")
    public class EventResource extends ResourceSerializedBase<Event,String> {
        @Autowired
        EventDao eventDao;

        @Autowired
        EventSerializer eventSerializer;

        @Autowired
        UserSerializer userSerializer;

        public EventResource() {
            super(Event.class);
        }

        @Override
        protected BaseDao<Event, String> getDao() {
            return this.eventDao;
        }

        @Override
        protected void addModuleSerializers(SimpleModule testModule){
            testModule.addSerializer(Event.class, eventSerializer);
            testModule.addSerializer(User.class, userSerializer);
        }
    }


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

## Property Configuration

It's possible to configure your application via an environment name which will match to a resource property file included in the classpath.  This is great if you dont mind including environment settings in your packaging.  For a full example, please see the [SpringEnvironmentPropertyPlaceholderConfigurerTest](./src/test/java/unit/com/makeandbuild/vessl/propconfig/SpringEnvironmentPropertyPlaceholderConfigurerTest.java).  You can also see the configuration of [src/test/resources/spring-propconfig.xml](./src/test/resources/spring-propconfig.xml).  Here is a snippet in $TOMCAT_HOME/bin/setenv.sh using the environment name (will load [/config-dev.properties](./src/test/resources/config-dev.properties) in classpath)

    JAVA_OPTS="-DenvironmentName=dev -Dlog4j.configuration=file:/home/dev/log4j.properties"
    export JAVA_OPTS

It's also possible to explicitly define the property file on the local filesystem you want to use.  If you are in dev/ops this is probably what you want to have for your produciton environmets.  Here is a snippet in $TOMCAT_HOME/bin/setenv.sh using the full filename

    JAVA_OPTS="-DenvironmentFilename=/home/dev/config-dev.properties -Dlog4j.configuration=file:/home/dev/log4j.properties"
    export JAVA_OPTS

## Integration Tests

Create your database

    mysql -u root
    create database vessl

Now create the user and event tables

    mysql -u root vessl < ./src/test/resources/create_user.sql
    mysql -u root vessl < ./src/test/resources/create_event.sql

Now you can run the function test

     mvn integration-test -Pft

For a bigger example please see the sibling web app project https://github.com/makeandbuild/vessl-webapp

## Including in your project

vessl is distributed in [maven central](https://search.maven.org/#browse%7C-958470226), you can find the latest versions there.  The current gradle entry (from [build.gradle](https://github.com/makeandbuild/vessl-webapp/blob/master/build.gradle)):

    compile 'com.makeandbuild:vessl:1.0.52'

and here for those of you that still use maven pom files for your projects:

    <dependency>
        <groupId>com.makeandbuild</groupId>
        <artifactId>vessl</artifactId>
        <version>1.0.52</version>
    </dependency>