# messy [![Java CI](https://github.com/marco-schmidt/messy/workflows/Java%20CI/badge.svg)](https://github.com/marco-schmidt/messy/actions?query=workflow%3A%22Java+CI%22)  [![CodeQL](https://github.com/marco-schmidt/messy/workflows/CodeQL/badge.svg)](https://github.com/marco-schmidt/messy/actions?query=workflow%3ACodeQL) [![Codecov](https://codecov.io/gh/marco-schmidt/messy/branch/master/graphs/badge.svg?branch=master)](https://codecov.io/gh/marco-schmidt/messy) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/ca8ac2c8c93748b5a6f659de8189294e)](https://www.codacy.com/gh/marco-schmidt/messy/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=marco-schmidt/messy&amp;utm_campaign=Badge_Grade) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A tool suite for electronic messages.

## Features

* Read mbox.
* Write messy tar files (set of tar files with one file per message).
* Parse A News.

## Status

Created November 8th, 2020.
As of 2020, a one-person hobby project.

## Goals

### Human Goals

* Help users sort through, triage, clean up and consolidate their messages as a basis for discovery, backup and archival.
* Enable digital preservation of public messages as a part of computing history.
* Simplify bulk exchange of messages between interested parties.

### Technological Goals

* Parse electronic messages of various types.
* Support different file formats.
* Read messages from servers with different protocols.
* Handle extraction of attachments and references to external information.
* Create a message database with full text search and reporting.
* Analyze messages to allow more fine-grained search, separate public from private ones.

## Technology Stack

* Written in Java 8, using AdoptOpenJDK (but any JDK version 8 or higher should do).
* Build tool gradle, as a multi-project build with the gradle wrapper.
* Hosted in a public git repository at GitHub.
* Continuous integration with GitHub Workflow Java CI.
* Dependencies:
    * JUnit for unit tests,
    * archive I/O from Apache Commons Compress,
    * logging with slf4j and logback.
* Static code analysis with
    * gradle plugins spotbugs, checkstyle and forbiddenApis and
    * service codacy.com.
* Project comes with an Eclipse configuration file and gradle is configured to generate a workspace for Eclipse. Any other Java IDE will probably also work.
* Code formatting and license header with gradle spotless plugin. Also format automatically when saving in Eclipse (if provided configuration file is used, see below for gradle Eclipse workspace setup).
* Vulnerability analysis:
    * Gradle plugin dependencyCheck. It compares direct and transitive dependencies to CVE entries in the National Vulnerability Database (NVD).
    * GitHub workflow service CodeQL.
* API documentation with javadoc.
* Code coverage reporting with jacoco and codecov.io.
* Check for new versions of dependencies with gradle plugin versions.
* Create reports of dependencies and their licenses and check against licenses against positive list.

## Development Setup

* Install JDK 8 or higher on the system.
* Set environment variable JAVA_HOME to the JDK installation path, include its bin subdirectory in PATH variable. Run javac -version and possbily which java to make sure that the right Java compiler and virtual machine are available now.
* Clone the messy git repository.
* Navigate to cloned working copy and run ./gradlew check as an initial toolchain check.
* Install Eclipse IDE, run ./gradlew eclipse, open Eclipse and import projects msg*.
