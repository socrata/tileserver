package com.socrata.tileserver
package mocks

import scala.collection.JavaConverters._
import scala.collection.mutable.Buffer
import java.nio.charset.StandardCharsets.UTF_8

import com.vividsolutions.jts.geom.Geometry
import no.ecc.vectortile.VectorTileEncoder

import util.TileEncoder.Feature

class StringEncoder extends VectorTileEncoder {
  private val underlying: Buffer[String] = Buffer()

  override def addFeature(layer: String,
                          attrs: java.util.Map[String, _],
                          geom: Geometry): Unit =
    underlying += (layer, attrs, geom).toString

  override def encode(): Array[Byte] = toString.getBytes(UTF_8)

  override def toString: String = underlying.sorted.toString
}

object StringEncoder {
  def apply(): StringEncoder = new StringEncoder()

  def encFeatures(features: Set[Feature]): String = {
    val enc = new StringEncoder()

    features foreach { case (pt, attrs) =>
      enc.addFeature("main", attrs.asJava, pt)
    }

    enc.toString
  }
}