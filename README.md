# Core Utils: tools to facilitate the development of Java-based applications

The _coreutils_ project contains several core libraries that deal with the following aspects of software development: providing common and extensible configuration across different application modules, executing ordered shutdown sequences when the JVM shutdowns, running tasks in a custom thread pool, allowing publish-subscribe-style communication between components without requiring them to explicitly register with one another, providing commonly-used clients (HTTP, REST) integrated with with green threads (fibers) to minimize the performance impact in virtual environments, installing logging bridges to unify logging across the entire application, maintaining testing groups and rules.

This project requires Java 1.8 or higher.

## Master Build Status

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://github.com/grycap/coreutils/blob/master/LICENSE)
[![Build Status](https://api.travis-ci.org/grycap/coreutils.svg)](https://travis-ci.org/grycap/coreutils/builds)
[![Coverage Status](https://coveralls.io/repos/grycap/coreutils/badge.svg?branch=master&service=github)](https://coveralls.io/github/grycap/coreutils?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/es.upv.grycap.coreutils/coreutils/badge.svg)](https://maven-badges.herokuapp.com/maven-central/es.upv.grycap.coreutils/coreutils)

## What is _coreutils_?

_Coreutils_ is a toolkit for developing highly concurrent and distributed applications in Java. Every new release starts from the available technologies and choose carefully a core of components that is configured in _coreutils_ to leverage their strengths and to simplify the development, testing and deployment of new Java applications.

_Coreutils_ is an open-source software that can be used as an SDK or can be taken as a code base for developing custom applications.

## What is not _coreutils_?

_Coreutils_ is not a framework, nor a utility library. _Coreutils_ puts a very thin layer on top of the underlying technologies, providing the necessary glue to integrate heterogeneous, already existing components into coherent applications. In many cases, _coreutils_ will expose parts of the original APIs in especial to avoid data transfer from different data formats or storages. This make assuring backward compatibility impossible.

For Java frameworks and utility libraries take a look at the following (incomplete) list:

* Spring Framework (http://projects.spring.io/spring-framework) is a full-featured framework with a focus on modern Java applications, in especial Spring Boot, Spring Data, Spring REST and Spring Cloud.

* Jodd micro frameworks (http://jodd.org) is suitable for small and medium-sized projects where keeping a small memory footprint is critical.

* Google Guava (https://github.com/google/guava) and Apache Commons (https://commons.apache.org) are the indisputable leader in the field of reusable Java components, providing common methods and behaviors that are widely used.

## Latest release

Add a dependency on _coreutils_ using Maven:

```xml
<dependency>
    <groupId>es.upv.grycap.coreutils</groupId>
    <artifactId>coreutils</artifactId>
    <version>0.2.0</version>
</dependency>
```

## Using or extending _coreutils_

Despite _Coreutils_ is an unopinionated toolkit, there are several aspects of the project configuration that deserve attention when using _coreutils_ to develop a custom application. _Coreutils_ includes testing groups and rules that can be used to isolate tests. These are used in the project itself to control the test output and to run the test groups in different build phases. _Coreutils_ uses Maven profiles that are activated via environment properties.

These notes are also valid for extending _coreutils_. 

### Environment variables

* ``grycap.tests.print.out`` set value to ``true`` to print tests output to the console and to write log messages to a temporary file. Otherwise, no messages are displayed.

* ``grycap.deploy.release`` set value to ``true`` to release the project to the Maven Central repository. This requires proper configuration of the OSSRH credentials and access to the project's repository. The script ``ossrh-deploy.sh`` provides further information about deployment.

### Run all tests, logging to the console

``$ mvn clean verify -Dgrycap.tests.print.out=true |& tee /tmp/LOGFILE``

To include only a specific submodule add the ``-pl`` to the arguments. For example, include the following to test only the submodule ``coreutils-common``: ``-pl coreutils-common``.

### Run sanity and functional tests, logging to the console

``$ mvn clean test -Dgrycap.tests.print.out=true |& tee /tmp/LOGFILE``

## Continuous integration

Continuous integration and code coverage are configured within the default Maven profile in the ``verify`` build phase.

``$ mvn clean verify coreutils``

## Install to local Maven repository

``$ mvn clean install coreutils``

## Examples (pending)

* Spring Boot + Undertow

## TO-DO list

1. Integrate naming tools.
2. HTTP2 client: get access to the underlying OkHttpClient instance by cloning the client.
3. Add more tests.