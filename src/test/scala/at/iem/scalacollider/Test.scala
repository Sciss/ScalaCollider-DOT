package at.iem.scalacollider

import de.sciss.synth._

object Test extends App {
  val config  = ScalaColliderDOT.Config()
  val analogBubbles = SynthDef("analog bubbles") {
    import ugen._
    val f   = LFSaw.kr(0.4).madd(24, LFSaw.kr(Seq(8.0, 7.23)).madd(3, 80)).midicps
    val sig = CombN.ar(SinOsc.ar(f) * 0.04, 0.2, 0.2, 4)
    Out.ar(0, sig)
  }

  val policeState = SynthDef("police state") {
    import ugen._
    val n = 4   // number of sirens
    val sig = CombL.ar(
      Mix.fill(n) {
        Pan2.ar(
          SinOsc.ar(
            SinOsc.kr(Rand(0.02, 0.12), Rand(0, 2*math.Pi)).madd(IRand(0, 599), IRand(700, 1299))
          ) * LFNoise2.ar(Rand(80, 120)) * 0.1,
          Rand(-1, 1)
        )
      }
        + LFNoise2.ar(
        LFNoise2.kr(Seq(0.4, 0.4)).madd(90, 620)) *
        LFNoise2.kr(Seq(0.3, 0.3)).madd(0.15, 0.18),
      0.3, 0.3, 3
    )
    WrapOut(sig)
  }

  def printDef(sd: SynthDef): Unit = {
    config.input      = sd.graph
    config.graphName  = sd.name
    config.rateColors = true
    val res           = ScalaColliderDOT(config)
    println(res)
  }

  printDef(analogBubbles)
  println()
  printDef(policeState)
}