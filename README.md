# ScalaCollider-DOT

[![Build Status](https://travis-ci.org/Sciss/ScalaCollider-DOT.svg?branch=master)](https://travis-ci.org/Sciss/ScalaCollider-DOT)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.sciss/scalacollider-dot_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.sciss/scalacollider-dot_2.12)

A utility that translates a [ScalaCollider](https://git.iem.at/sciss/ScalaCollider) UGen graph into a GraphViz .dot file.
This project is (C)opyright 2016&ndash;2017 by the Institute of Electronic Music and Acoustics (IEM), Graz.
(C)opyright 2017&ndash;2019 by Hanns Holger Rutz. 
This software is published under the GNU Lesser General Public License v2.1+.

See `src/test` for an example usage.

![example](example.png)

## linking

The following artifact is available from Maven Central:

    "de.sciss" %% "scalacollider-dot" % v

The current stable version `v` is `"0.10.3"`.

## building

This project builds with sbt and Scala 2.13, 2.12, 2.11. To compile `sbt test:compile`.
To print the test output, `sbt test:run`.

## contributing

Please see the file [CONTRIBUTING.md](CONTRIBUTING.md)
