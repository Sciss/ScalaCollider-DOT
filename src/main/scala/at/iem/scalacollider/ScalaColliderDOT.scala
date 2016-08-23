/*
 *  Dimension.scala
 *  (ScalaCollider-DOT)
 *
 *  Copyright (c) 2016 Institute of Electronic Music and Acoustics, Graz.
 *  Written by Hanns Holger Rutz.
 *
 *	This software is published under the GNU Lesser General Public License v2.1+
 *
 *
 *	For further information, please contact Hanns Holger Rutz at
 *	contact@sciss.de
 */

package at.iem.scalacollider

import java.io.{File, FileOutputStream}

import de.sciss.synth.UGenGraph

import scala.language.implicitConversions

object ScalaColliderDOT {
  sealed trait ConfigLike {
    /** The UGen graph to render */
    def input: UGenGraph
    /** Whether the UGen name should be rendered in bold font. */
    def nameBoldFont : Boolean
    /** Font size for the UGen name. Zero for default size. */
    def nameFontSize : Int
    /** HTML color for the UGen name. Empty for default color. */
    def nameFontColor: String
    /** HTML color for constant argument values. Empty for default color. */
    def constantFontColor: String
  }

  object Config {
    def apply(): ConfigBuilder = new ConfigBuilder

    implicit def build(b: ConfigBuilder): Config =
      new Config(input = b.input, nameBoldFont = b.nameBoldFont, nameFontSize = b.nameFontSize,
        nameFontColor = b.nameFontColor, constantFontColor = b.constantFontColor)
  }
  final case class Config(input: UGenGraph, nameBoldFont: Boolean, nameFontSize: Int, nameFontColor: String,
                          constantFontColor: String) extends ConfigLike

  final class ConfigBuilder extends ConfigLike {
    var input             : UGenGraph = UGenGraph(Vector.empty, Vector.empty, Vector.empty, Vector.empty)
    var nameBoldFont      : Boolean   = true
    var nameFontSize      : Int       = 18
    var nameFontColor     : String    = ""
    var constantFontColor : String    = "blue"
  }

  /** Renders to DOT, writing it to a given output file. */
  def writeDOT(config: Config, out: File): Unit = {
    val contents = apply(config)
    val f = new FileOutputStream(out)
    try {
      f.write(contents.getBytes("UTF-8"))
    } finally {
      f.close()
    }
  }

  /** Renders to PDF using the `dot` shell command. */
  def writePDF(config: Config, out: File): Unit = {
    val dotFile = File.createTempFile("temp", "dot")
    try {
      writeDOT(config, dotFile)
      import scala.sys.process._
      val res = Seq("dot", "-Tpdf", dotFile.getPath).#>(out).!
      if (res != 0) sys.error(s"'dot' returned with code $res")

    } finally {
      dotFile.delete()
    }
  }

  /** Renders to DOT, returning it as a string. */
  def apply(config: Config): String = {

    ???
  }
}