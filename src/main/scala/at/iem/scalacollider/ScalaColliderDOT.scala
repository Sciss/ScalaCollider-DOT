/*
 *  Dimension.scala
 *  (ScalaCollider-DOT)
 *
 *  Copyright (c) 2016 Institute of Electronic Music and Acoustics, Graz.
 *  Copyright (c) 2017-2018 by Hanns Holger Rutz.
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

import de.sciss.synth.UGenSpec.{Argument, ArgumentType, Input, Output, SignalShape}
import de.sciss.synth.ugen.{BinaryOpUGen, Constant, UnaryOpUGen}
import de.sciss.synth.{UGenGraph, UGenSpec, UndefinedRate, audio, control, demand, scalar}

import scala.collection.SortedMap
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
    /** Boolean indicating whether to use different colors for the UGen computation rates or not. Default is false. */
    def rateColors: Boolean
  }

  object Config {
    def apply(): ConfigBuilder = new ConfigBuilder

    implicit def build(b: ConfigBuilder): Config =
      new Config(input = b.input, graphName = b.graphName,
        nameBoldFont = b.nameBoldFont, nameFontSize = b.nameFontSize,
        nameFontColor = b.nameFontColor, constantFontColor = b.constantFontColor,
        constantDefaultFontColor = b.constantDefaultFontColor, rateColors = b.rateColors)
  }
  final case class Config(input: UGenGraph, graphName: String,
                          nameBoldFont: Boolean, nameFontSize: Int, nameFontColor: String,
                          constantFontColor: String, constantDefaultFontColor: String, rateColors: Boolean)
    extends ConfigLike

  final class ConfigBuilder extends ConfigLike {
    /** The default UGen graph is empty. */
    var input                     : UGenGraph = UGenGraph(Vector.empty, Vector.empty, Vector.empty, Vector.empty)
    /** The default name is `"UGenGraph"`. */
    var graphName                 : String    = "UGenGraph"
    /** The default is to have a bold name font. */
    var nameBoldFont              : Boolean   = true
    /** The default is to have font size 16 for the UGen name. */
    var nameFontSize              : Int       = 16
    var nameFontColor             : String    = ""
    /** The default is to have blue color for constant arguments. */
    var constantFontColor         : String    = "blue"
    /** The default is to have gray color for default constants. */
    var constantDefaultFontColor  : String    = "#707070"
    /** The default is to not indicate UGen rate by color. */
    var rateColors                : Boolean   = false
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
      var res = UGenSpec.standardUGens ++ UGenSpec.thirdPartyUGens
      // cf. https://github.com/Sciss/ScalaColliderUGens/issues/36
      if (!res.contains("MulAdd")) res += ("MulAdd" ->
        UGenSpec("MulAdd", Set.empty, UGenSpec.Rates.Set(Set(audio, control, scalar)),
          args = Vector(
            Argument("in" , ArgumentType.GE(SignalShape.Generic), defaults = Map.empty, rates = Map.empty),
            Argument("mul", ArgumentType.GE(SignalShape.Generic), defaults = Map.empty, rates = Map.empty),
            Argument("add", ArgumentType.GE(SignalShape.Generic), defaults = Map.empty, rates = Map.empty)
          ), inputs = Vector(
            Input("in", Input.Single), Input("mul", Input.Single), Input("add", Input.Single)
          ), outputs = Vector(
            Output(name = None, shape = SignalShape.Generic, variadic = None)
          ), doc = None, elemOption = None)
        )
      if (!res.contains("UnaryOpUGen")) res += ("UnaryOpUGen" ->
        UGenSpec("UnaryOpUGen", Set.empty, UGenSpec.Rates.Set(Set(audio, control, scalar)),
          args = Vector(
            Argument("in", ArgumentType.GE(SignalShape.Generic), defaults = Map.empty, rates = Map.empty)
          ), inputs = Vector(
            Input("in", Input.Single)
          ), outputs = Vector(
            Output(name = None, shape = SignalShape.Generic, variadic = None)
          ), doc = None, elemOption = None)
        )
      if (!res.contains("BinaryOpUGen")) res += ("BinaryOpUGen" ->
          UGenSpec("BinaryOpUGen", Set.empty, UGenSpec.Rates.Set(Set(audio, control, scalar)),
            args = Vector(
              Argument("a", ArgumentType.GE(SignalShape.Generic), defaults = Map.empty, rates = Map.empty),
              Argument("b", ArgumentType.GE(SignalShape.Generic), defaults = Map.empty, rates = Map.empty)
            ), inputs = Vector(
              Input("a", Input.Single), Input("b", Input.Single)
            ), outputs = Vector(
              Output(name = None, shape = SignalShape.Generic, variadic = None)
            ), doc = None, elemOption = None)
        )
      res
    } catch {
      case NonFatal(e) =>
        Console.err.println("While initializing UGen specs:")
        e.printStackTrace()
        Map.empty
    }

  private def escapeHTML(in: String): String =
    in.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")

  private def escapeName(in: String): String =
    in.replaceAll(" ", "_").replaceAll("-", "_")

  /** Renders to DOT, returning it as a string. */
  def apply(config: Config): String = {
    import config._
    val cs = input.constants
    val cnB = SortedMap.newBuilder[Int, String]
    val cnV = input.controlNames
    cnB.sizeHint(cnV.size)
    cnV.foreach { tup =>
      cnB += tup.swap
    }
    val cn = cnB.result()
    val nf = NumberFormat.getInstance(Locale.US)
    nf.setMaximumFractionDigits(4)
    nf.setGroupingUsed(false)

    val nodeBuilder = new StringBuilder
    val edgeBuilder = new StringBuilder
    nodeBuilder.append(s"digraph ${escapeName(graphName)} {\n")
    val nl          = "\n"

    input.ugens.zipWithIndex.foreach { case (iu, iui) =>
      val ugenName0 = iu.ugen.name
      val numIns    = iu.ugen.numInputs
      val numOuts   = iu.ugen.numOutputs
      val specOpt   = ugenMap.get(ugenName0)

      val nodeLabel = s"ugen$iui"
      nodeBuilder.append(s"  node [shape=plaintext] $nodeLabel [label=<\n")
      nodeBuilder.append( "      <TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\">\n")
      nodeBuilder.append( "      <TR>\n")
      if (numIns > 0) {
        iu.inputSpecs.zipWithIndex.foreach {
          case ((ugenIdx, outIdx), inIdx) =>
            val inName = specOpt.flatMap { spec =>
              val ins = spec.inputs
              if (inIdx < ins.size) {
                val in = ins(inIdx)
                Some(if (in.variadic) s"${in.arg}0" else in.arg)
              } else ins.lastOption.flatMap { in =>
                if (in.variadic) Some(s"${in.arg}${inIdx - (ins.size - 1)}") else None
              }
            } getOrElse (if (numIns == 1) "in" else s"in$inIdx")

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
                    defOpt.exists(_.toGE == Constant(c))
                  }
                }
                if (isDefault) constantDefaultFontColor else constantFontColor
              }
              if (colr.isEmpty) s"$inName: $cStr" else s"""$inName: <FONT COLOR="$colr">$cStr</FONT>"""
            }

            val inLabel = s"in$inIdx"
            nodeBuilder.append(s"""        <TD PORT="$inLabel">$inCell</TD>$nl""")

            if (!isConstant) {
              edgeBuilder.append(s"  ugen$ugenIdx:out$outIdx -> $nodeLabel:$inLabel;\n")
            }
        }
        nodeBuilder.append("      </TR><TR>\n")
      }
      val ugenName1 = ugenName0 match {
        case "UnaryOpUGen"  => UnaryOpUGen .Op(iu.ugen.specialIndex).name.toLowerCase(Locale.US)
        case "BinaryOpUGen" => BinaryOpUGen.Op(iu.ugen.specialIndex).name.toLowerCase(Locale.US)
        case other => other
      }
      val ugenName = escapeHTML(ugenName1)
      val nameCell0 = if (!nameBoldFont)     ugenName else s"<B>$ugenName</B>"
      val nameCell  = if (nameFontSize == 0) nameCell0 else s"""<FONT POINT-SIZE="$nameFontSize">$nameCell0</FONT>"""
      val nameCellBg = if (!rateColors)      "" else {
        val colrName = iu.ugen.rate match {
          case `audio`    => "#F0B0B0"
          case `control`  => "#C0C0FF"
          case `scalar`   => "#D8D8D8"
          case `demand`   => "#B0F0B0"
        }
        s""" BGCOLOR="$colrName""""
      }
      nodeBuilder.append(s"""        <TD COLSPAN="${math.max(1, math.max(numIns, numOuts))}"$nameCellBg>$nameCell</TD>$nl""")
      if (numOuts > 0) {
        nodeBuilder.append("      </TR><TR>\n")
        for (outIdx <- 0 until numOuts) {
          val outNameOpt = if (ugenName0 == "Control" || ugenName == "AudioControl" || ugenName == "TrigControl") {
            val ctlIdx  = iu.ugen.specialIndex + outIdx
            val lastOpt = cn.until(ctlIdx + 1).lastOption
            lastOpt.collect {
              case (startIdx, ctlName) if startIdx + numOuts > ctlIdx => ctlName
            }
          }
          else specOpt.flatMap { spec =>
            val outs = spec.outputs
            if (outIdx < outs.size) {
              val out = outs(outIdx)
              val n = out.name.getOrElse("out")
              Some(if (out.variadic.isDefined) s"$n$outIdx" else n)
            } else outs.lastOption.flatMap { out =>
              val n = out.name.getOrElse("out")
              if (out.variadic.isDefined) Some(s"$n${outIdx - (outs.size - 1)}") else None
            }
          }
          val outName = outNameOpt.getOrElse(if (numOuts == 1) "out" else s"out$outIdx")
          val outLabel = s"out$outIdx"
          nodeBuilder.append(s"""        <TD PORT="$outLabel">$outName</TD>$nl""")
        }
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