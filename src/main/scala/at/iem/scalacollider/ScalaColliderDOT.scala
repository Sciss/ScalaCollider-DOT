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
import java.text.NumberFormat
import java.util.Locale

import de.sciss.synth.ugen.Constant
import de.sciss.synth.{UGenGraph, UGenSpec, UndefinedRate}

import scala.language.implicitConversions
import scala.util.control.NonFatal

object ScalaColliderDOT {
  sealed trait ConfigLike {
    /** The UGen graph to render */
    def input: UGenGraph
    /** Name of the graph */
    def graphName: String
    /** Whether the UGen name should be rendered in bold font. */
    def nameBoldFont : Boolean
    /** Font size for the UGen name. Zero for default size. */
    def nameFontSize : Int
    /** HTML color for the UGen name. Empty for default color. */
    def nameFontColor: String
    /** HTML color for constant argument values. Empty for default color. */
    def constantFontColor: String
    /** HTML color for constant argument values that correspond to the UGen's defaults. Empty for default color. */
    def constantDefaultFontColor: String
  }

  object Config {
    def apply(): ConfigBuilder = new ConfigBuilder

    implicit def build(b: ConfigBuilder): Config =
      new Config(input = b.input, graphName = b.graphName,
        nameBoldFont = b.nameBoldFont, nameFontSize = b.nameFontSize,
        nameFontColor = b.nameFontColor, constantFontColor = b.constantFontColor,
        constantDefaultFontColor = b.constantDefaultFontColor)
  }
  final case class Config(input: UGenGraph, graphName: String,
                          nameBoldFont: Boolean, nameFontSize: Int, nameFontColor: String,
                          constantFontColor: String, constantDefaultFontColor: String) extends ConfigLike

  final class ConfigBuilder extends ConfigLike {
    var input                     : UGenGraph = UGenGraph(Vector.empty, Vector.empty, Vector.empty, Vector.empty)
    var graphName                 : String    = "UGenGraph"
    var nameBoldFont              : Boolean   = true
    var nameFontSize              : Int       = 18
    var nameFontColor             : String    = ""
    var constantFontColor         : String    = "blue"
    var constantDefaultFontColor  : String    = "gray"
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

  private lazy val ugenMap: Map[String, UGenSpec] = try {
      UGenSpec.standardUGens ++ UGenSpec.thirdPartyUGens
    } catch {
      case NonFatal(e) =>
        Console.err.println("While initializing UGen specs:")
        e.printStackTrace()
        Map.empty
    }

  /** Renders to DOT, returning it as a string. */
  def apply(config: Config): String = {
    import config._
    val cs = input.constants
    val nf = NumberFormat.getInstance(Locale.US)
    nf.setMaximumFractionDigits(4)

    val nodeBuilder = new StringBuilder
    val edgeBuilder = new StringBuilder
    nodeBuilder.append(s"digraph $graphName {\n")
    val nl          = "\n"

    input.ugens.zipWithIndex.foreach { case (iu, iui) =>
      val ugenName  = iu.ugen.name
      val numIns    = iu.ugen.numInputs
      val numOuts   = iu.ugen.numOutputs
      val specOpt   = ugenMap.get(ugenName)

      val nodeLabel = s"ugen$iui"
      nodeBuilder.append(s"  node [shape=plaintext] $nodeLabel [label=<\n")
      nodeBuilder.append( "      <TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\">\n")
      nodeBuilder.append( "      <TR>\n")
      iu.inputSpecs.zipWithIndex.foreach {
        case ((ugenIdx, outIdx), inIdx) =>
          val inName = specOpt.flatMap { spec =>
            val ins = spec.inputs
            if (inIdx < ins.size) {
              val in = ins(inIdx)
              Some(if (in.variadic) s"${in.arg}0" else in.arg)
            } else ins.lastOption.flatMap { in =>
              if (in.variadic) Some(s"${in.arg}${inIdx - ins.size - 1}") else None
            }
          } getOrElse s"in$inIdx"

          val isConstant = ugenIdx < 0
          val inCell = if (!isConstant) inName else {
            val c     = cs(outIdx)
            val cStr  = nf.format(c)
            val colr  = specOpt.fold("") { spec =>
              val ins = spec.inputs
              val isDefault = inIdx < ins.size && {
                val in = ins(inIdx)
                spec.argMap.get(in.arg).exists { arg =>
                  val defOpt = arg.defaults.get(iu.ugen.rate).orElse(arg.defaults.get(UndefinedRate))
                  defOpt.map(_.toGE).contains(Constant(c))
                }
              }
              if (isDefault) constantDefaultFontColor else constantFontColor
            }
            if (colr.isEmpty) s"$inName: $cStr" else s"""$inName: <FONT COLOR="$colr">$cStr</FONT>"""
          }

          val inLabel = s"in$inIdx"
          nodeBuilder.append(s"""        <TD PORT="$inLabel">$inCell</TD>$nl""")

          if (!isConstant) {
            edgeBuilder.append(s"ugen$ugenIdx:out$outIdx -> $nodeLabel:$inLabel;\n")
          }
      }
      nodeBuilder.append("      </TR><TR>\n")
      val ugenName1 = ugenName  // XXX TODO --- replace UnaryOpUGen and BinaryOpUGGen
      val nameCell0 = if (!nameBoldFont)     ugenName1 else s"<B>$ugenName1</B>"
      val nameCell  = if (nameFontSize == 0) nameCell0 else s"""<FONT POINT-SIZE="$nameFontSize">$nameCell0</FONT>"""
      nodeBuilder.append(s"""        <TD COLSPAN="${math.max(1, math.max(numIns, numOuts))}">$nameCell</TD>$nl""")
      nodeBuilder.append("      </TR><TR>\n")
      for (outIdx <- 0 until numOuts) {
        val outName = specOpt.flatMap { spec =>
          val outs = spec.outputs
          if (outIdx < outs.size) {
            val out = outs(outIdx)
            Some(if (out.variadic.isDefined) s"${out.name}$outIdx" else out.name)
          } else outs.lastOption.flatMap { out =>
            if (out.variadic.isDefined) Some(s"$out.name}${outIdx - outs.size - 1}") else None
          }
        }  getOrElse (if (numOuts == 1) "out" else s"out$outIdx")
        val outLabel = s"out$outIdx"
        nodeBuilder.append(s"""        <TD PORT="$outLabel">$outName</TD>$nl""")
      }
      nodeBuilder.append("      </TR>\n")
      nodeBuilder.append("      </TABLE>\n")
      nodeBuilder.append("    >];\n\n")
    }

    nodeBuilder.append(edgeBuilder)
    nodeBuilder.append("}")

    nodeBuilder.toString()
  }
}