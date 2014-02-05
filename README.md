overview
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

For an example of the usage see UserDao_IT


setup for function tests
------------------

create your database
```mysql -u root
create database mnb_persistence```

now load the user table
```mysql -u root mnb_persistence < src/test/resource/create_user.sql```

now you can run the function test
```mvn integration-test -Pft```