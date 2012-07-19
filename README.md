What is Conte
==========

Conte is a Java implementation of ActiveRecord pattern that is just like the ActiveRecord in Rails.

#Usage

Make entity class to extends `ActiveRecord` class.


# Examples

##Insert

```java
Person person = new Person();
person.setName("mike");
person.setAge(20);
person.save(); // or Person.saveOrUpdate();
```
##Update

```java
Person person = Person.find(Person.class,1);
person.setName("mike");
person.setAge(20);
person.save(); // or Person.saveOrUpdate();
```

##Delete
```java
Person person = Person.find(Person.class,1);
person.delete();
```


###To be continued...



