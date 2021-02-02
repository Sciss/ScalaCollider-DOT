# ScalaCollider-DOT

[![Build Status](https://github.com/Sciss/ScalaCollider-DOT/workflows/Scala%20CI/badge.svg?branch=main)](https://github.com/Sciss/ScalaCollider-DOT/actions?query=workflow%3A%22Scala+CI%22)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.sciss/scalacollider-dot_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.sciss/scalacollider-dot_2.13)

A utility that translates a [ScalaCollider](https://git.iem.at/sciss/ScalaCollider) UGen graph into a GraphViz .dot file.
This project is (C)opyright 2016&ndash;2021 by Hanns Holger Rutz. All rights reserved.
It is published under the GNU Affero General Public License v3+.

See `src/test` for an example usage.

![example](example.png)

## linking

The following artifact is available from Maven Central:

    "de.sciss" %% "scalacollider-dot" % v

The current stable version `v` is `"1.6.1"`.

## building

This project builds with sbt and Scala 2.13, 2.12, Dotty.
The last version to support Scala 2.11 was v0.10.4.

To compile `sbt test:compile`. To print the test output, `sbt test:run`.

## contributing

Please see the file [CONTRIBUTING.md](CONTRIBUTING.md)
