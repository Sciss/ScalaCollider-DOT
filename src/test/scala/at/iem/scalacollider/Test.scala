package at.iem.scalacollider

import de.sciss.synth._

object Test extends App {
  val config  = ScalaColliderDOT.Config()
  val sd      = SynthDef("test") {
    import ugen._
    val f   = LFSaw.kr(0.4).madd(24, LFSaw.kr(Seq(8.0, 7.23)).madd(3, 80)).midicps
    val sig = CombN.ar(SinOsc.ar(f) * 0.04, 0.2, 0.2, 4)
    Out.ar(0, sig)
  }
  config.input      = sd.graph
  config.graphName  = sd.name
  val res           = ScalaColliderDOT(config)

  println(res)
}
