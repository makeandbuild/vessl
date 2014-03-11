jdbc based persistence
--------

this framework is an extension of the spring jdbc implementation.  it provided the following services:
* makes use of jpa annotations in your model class.  See User.java as an example
* can use the simplified ReflectionBasedJdbcMapper to easily create your DAOs.  This supports all sorts of types including Date, Integer, Long, Enums (as string column mappings), String.  See UserDaoImpl as an example
* if you dont want the overhead of Reflection you can create those implementations as well

the BaseDao has a lot of built in methods including
* criteria based finders
* paging
* sorting
* find by id
* exists finders
* delete helpers
* supports domain model specialization inheritance (see the @Specialize annotation and the UserDao_IT.testSpecialized() test)

For an example of the usage see UserDao_IT

fixtures
-------

you can also make use of the fixture functionality to load test data from class resources.  the solution this is based upon seperates loaders from persisters and takes into account order of data that is being loaded.  if you have cross referencing associations, then you will probably want to write your own custom EntityLoaders and EntityManagers.

* src/test/resources/fixtures includes json resources to be loaded
* src/test/java/integration/com/makeandbuild/fixture/Fixture_IT.java has the tests for loading and purging data
* src/test/resources/spring.xml definition of fixture sets up the meta data for the project and takes into account the ordering

there are some tests
* testAll() demonstrates how you can setup a set of resource files to be loaded in spring and purge() or load() as a complete set
* testResourceSingularly() demonstrates on how to pass in a resource location to load a resoruce explicity
* testEntityClassSingularly() demonstrates how to purge via a given Model class


validation
-------

Validation is supported via JSR-303 annotations and custom spring Validator instances you define. Simply defining an
instance of ValidationProxyManagerImpl in the application context will suffice to load (and cache) all custom
Validators you have also defined in the application context. Validation does not occur by default on any of your Dao
classes. To enable bean validation you should create a dynamic proxy instance of your Dao (make sure it extends
BaseDao) and perform your methods through that proxy. All calls made through your dao proxy instance will have its
parameters ran through a Validator instance that supports the parameters object type. Multiple validators can be ran
against the same object. This is controlled by the supports(...) method of your custom Validator instance. If a
validation errors is encountered the proxy dao instance will throw a RuntimeException (BeanValidationException) that
contains a list of ObjectError objects defining the validation errors that occured.

setup for function tests
------------------

create your database

    mysql -u root
    create database mnb_persistence

now load the user table

    mysql -u root mnb_persistence < ./src/test/resources/create_user.sql
    mysql -u root mnb_persistence < ./src/test/resources/create_event.sql

now you can run the function test

     mvn integration-test -Pft