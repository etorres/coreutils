# Core Utils
Utilities to facilitate Java applications development.

## Master Build Status

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://github.com/grycap/coreutils/blob/master/LICENSE)
[![Build Status](https://api.travis-ci.org/grycap/coreutils.svg)](https://travis-ci.org/grycap/coreutils/builds)
[![Coverage Status](https://coveralls.io/repos/grycap/coreutils/badge.svg?branch=master&service=github)](https://coveralls.io/github/grycap/coreutils?branch=master)

## Environment variables

``GRYCAP_TESTS_PRINT_OUTPUT`` set value to ``true`` to print tests output.

## Installation

### Install from source

``$ mvn clean install coreutils``

## Development

### Run all tests logging to the console

``$ mvn clean verify -pl coreutils-core -Dgrycap.tests.print.out=true |& tee /tmp/LOGFILE``

### Run functional and sanity tests logging to the console

``$ mvn clean test -pl coreutils-core -Dgrycap.tests.print.out=true |& tee /tmp/LOGFILE``

## Continuous integration

``$ mvn clean verify coreutils``

