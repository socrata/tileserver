package com.socrata.tileserver

import scala.language.implicitConversions

import javax.servlet.http.HttpServletResponse.{SC_NOT_MODIFIED => ScNotModified}
import javax.servlet.http.HttpServletResponse.{SC_OK => ScOk}

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

package object implicits {
  object StatusCodes {
    case class KnownStatusCode(val underlying: Int) {
      override val toString: String = underlying.toString
    }
    implicit def knownStatusCodeToInt(k: KnownStatusCode): Int = k.underlying

    case class UnknownStatusCode(val underlying: Int) {
      override val toString: String = underlying.toString
    }
    implicit def unknownStatusCodeToInt(u: UnknownStatusCode): Int = u.underlying

    // scalastyle:off magic.number
    private val knownStatusCodes = Set(400, 403, 404, 408, 500, 501, 503)

    private val knownScGen = for {
      statusCode <- Gen.oneOf(knownStatusCodes.toSeq)
    } yield (KnownStatusCode(statusCode))

    private val unknownScGen = for {
      statusCode <- Gen.choose(100, 599) suchThat { statusCode: Int =>
        !knownStatusCodes(statusCode) && statusCode != ScOk && statusCode != ScNotModified
      }
    } yield (UnknownStatusCode(statusCode))
    // scalastyle:on magic.number
    implicit val knownSc: Arbitrary[KnownStatusCode] = Arbitrary(knownScGen)
    implicit val unknownSc: Arbitrary[UnknownStatusCode] = Arbitrary(unknownScGen)
  }

  object Headers {
    sealed trait Header {
      def key: String
      def value: String
    }

    case class IncomingHeader(key: String, value: String) extends Header
    case class OutgoingHeader(key: String, value: String) extends Header
    case class UnknownHeader(key: String, value: String) extends Header
    implicit def headerToTuple(h: Header): (String, String) = (h.key, h.value)

    private val socrata = "X-Socrata-"
    private val incoming = Set("Authorization",
                               "Cookie",
                               "If-Modified-Since",
                               socrata)
    private val outgoing = Set("Cache-Control",
                               "Expires",
                               "Last-Modified",
                               socrata)

    private val incomingGen = for {
      k <- Gen.oneOf(incoming.toSeq)
      k2 <- Arbitrary.arbString.arbitrary
      v <- Arbitrary.arbString.arbitrary
    } yield if (k == socrata) IncomingHeader(k + k2, v) else IncomingHeader(k, v)

    private val outgoingGen = for {
      k <- Gen.oneOf(outgoing.toSeq)
      k2 <- Arbitrary.arbString.arbitrary
      v <- Arbitrary.arbString.arbitrary
    } yield if (k == socrata) OutgoingHeader(k + k2, v) else OutgoingHeader(k, v)

    private val unknownGen = for {
      k <- Arbitrary.arbString.arbitrary suchThat { k =>
        !(k.startsWith(socrata) || incoming(k) || outgoing(k))
      }
      v <- Arbitrary.arbString.arbitrary
    } yield UnknownHeader(k, v)

    implicit val incomingHeader: Arbitrary[IncomingHeader] = Arbitrary(incomingGen)
    implicit val outgoingHeader: Arbitrary[OutgoingHeader] = Arbitrary(outgoingGen)
    implicit val unknownHeader: Arbitrary[UnknownHeader] = Arbitrary(unknownGen)
  }

  object Points {
    sealed trait Point {
      def x: Int
      def y: Int
    }

    case class ValidPoint(x: Int, y: Int) extends Point
    case class InvalidPoint(x: Int, y: Int) extends Point

    implicit def pointToTuple(pt: Point): (Int, Int) = (pt.x, pt.y)
    // scalastyle:off magic.number
    private val validGen = for {
      x <- Gen.choose(0, 255)
      y <- Gen.choose(0, 255)
    } yield ValidPoint(x, y)

    private val invalidGen = for {
      x <- Gen.choose(-256, 512) suchThat { x => x < 0 || x > 255 }
      y <- Gen.choose(-256, 512) suchThat { y => y < 0 || y > 255 }
    } yield InvalidPoint(x, y)
    // scalastyle:on magic.number

    implicit val valid: Arbitrary[ValidPoint] = Arbitrary(validGen)
    implicit val invalid: Arbitrary[InvalidPoint] = Arbitrary(invalidGen)
  }
}