# messy [![Java CI](https://github.com/marco-schmidt/messy/workflows/Java%20CI/badge.svg)](https://github.com/marco-schmidt/messy/actions?query=workflow%3A%22Java+CI%22)  [![CodeQL](https://github.com/marco-schmidt/messy/workflows/CodeQL/badge.svg)](https://github.com/marco-schmidt/messy/actions?query=workflow%3ACodeQL) [![Codecov](https://codecov.io/gh/marco-schmidt/messy/branch/master/graphs/badge.svg?branch=master)](https://codecov.io/gh/marco-schmidt/messy) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/ca8ac2c8c93748b5a6f659de8189294e)](https://www.codacy.com/gh/marco-schmidt/messy/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=marco-schmidt/messy&amp;utm_campaign=Badge_Grade) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![Snyk Vulnerabilities](https://snyk.io/test/github/marco-schmidt/messy/badge.svg?targetFile=build.gradle)](https://snyk.io/test/github/marco-schmidt/messy?targetFile=build.gradle)

A tool suite for electronic messages.

## Features

### Input Formats

Messy recursively reads archive and container formats and parses several types of messages. Typically, one or more messages are stored in a file using a container format. One or more of those container files are then stored within an archive.

#### Archive Formats

These are general-purpose archive formats, not specific to messages.
* Single compressed files
    * Gzip (.gz)
    * Bzip2 (.bz2)
    * Compress (.Z)
* Multiple files stored without compression
    * Tar (.tar)
* Multiple files stored with compression
    * Zip (.zip)

#### Container Formats

* [Mbox](https://en.wikipedia.org/wiki/Mbox) files.
    * Supports various subtypes.
* [Newline-delimited JSON (ndjson)](http://ndjson.org/) files.
* Single-message files.
     * File extensions .eml and .msg.
     * Newsspool messages, no file extension, name is an integer number.

#### Message Formats

* [Internet Message Format (IMF)](https://en.wikipedia.org/wiki/Email#Message_format) used with email and Usenet messages.
* [A News](https://en.wikipedia.org/wiki/A_News) messages, a 1980s format for Usenet messages.
* [JSON tweets](https://developer.twitter.com/en/docs/twitter-api/v1/data-dictionary/overview) distributed as a directory tree of , each compressed with bzip2, the directory tree then packed in a single tar archive file.

### Storage

* Store messages in a [Lucene](https://lucene.apache.org/) search index directory.

## Status

Created November 8th, 2020.
As of 2021, a one-person hobby project.
Command-line application msgcli can be used to explore message archives, converting messages to JSON and printing them to standard output. 

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

## Command-Line Application

Command-line application ``msgcli`` reads messages from standard input, converts them and prints a summary of each message to standard output.

Clone the git repository and install msgcli locally:
```shell
./gradlew :msgcli:install
```
Then the ``/path/to/installed/msgcli/bin/msgcli`` placeholder in the following example can be replaced with ``msgcli/build/install/msgcli/bin/msgcli``.

This makes tar copy all the .json.bz2 files contained in a typical twitter stream archive to standard output,
lets bzip2 decompress that .bz2 data and send the resulting newline-delimited JSON text lines to msgcli, which will then convert them and print them to standard output:
```shell
tar -xOf /path/to/twitter-stream-2017-07-01.tar|bzip2 -d|/path/to/installed/msgcli/bin/msgcli
```
More recently zip has become the format of choice:
```shell
unzip -p /path/to/twitter-stream-2021-01-01.zip|/path/to/installed/msgcli/bin/msgcli
```

## Technology Stack

* Written in Java 8, using [Adoptium](https://adoptium.net) (but any JDK version 8 or higher should do).
* Build tool [gradle](https://gradle.org/), as a multi-project build with the gradle wrapper.
* Hosted in a public git repository at [GitHub](https://github.com/).
* Continuous integration with [GitHub Workflow Java CI](https://docs.github.com/en/actions/automating-builds-and-tests/building-and-testing-java-with-gradle).
* Dependencies:
    * [JUnit](https://junit.org/) for unit tests,
    * archive I/O from [Apache Commons Compress](https://commons.apache.org/proper/commons-compress/),
    * MIME support from [Jakarta Mail](https://eclipse-ee4j.github.io/mail/),
    * logging with [SLF4J](http://www.slf4j.org) and [Logback](http://logback.qos.ch),
    * [Lucene](https://lucene.apache.org) for full-text search.
* Static code analysis with
    * gradle plugins [SpotBugs](https://spotbugs.readthedocs.io/en/stable/gradle.html), [checkstyle](https://docs.gradle.org/current/userguide/checkstyle_plugin.html) and [Forbidden API Checker](https://plugins.gradle.org/plugin/de.thetaphi.forbiddenapis) and
    * service [Codacy](https://app.codacy.com/gh/marco-schmidt/messy/issues).
* Project comes with an Eclipse configuration file and gradle is configured to generate a workspace for Eclipse. Any other Java IDE will probably also work.
* Code formatting and license header with gradle spotless plugin. Also format automatically when saving in Eclipse (if provided configuration file is used, see below for gradle Eclipse workspace setup).
* Vulnerability analysis:
    * Gradle plugin dependencyCheck. It compares direct and transitive dependencies to CVE entries in the National Vulnerability Database (NVD).
    * GitHub workflow service CodeQL.
* API documentation with javadoc.
* Code coverage reporting with jacoco and codecov.io.
* Check for new versions of dependencies with gradle plugin versions.
* Create reports of dependencies and their licenses and check licenses against positive list.

## Development Setup

* Install JDK 8 or higher on the system.
* Set environment variable JAVA_HOME to the JDK installation path, include its bin subdirectory in PATH variable. Run ``javac -version`` and possibly ``which java`` to make sure that the right Java compiler and virtual machine are available now.
* Clone the messy git repository.
* Navigate to cloned working copy and run ``./gradlew check`` as an initial toolchain check.
* Install Eclipse IDE, run ``./gradlew eclipse`` in the cloned working copy, open Eclipse and import projects msg*.
