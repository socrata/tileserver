package com.socrata.tileserver.util

import java.io.InputStream
import java.nio.charset.{Charset, StandardCharsets}
import javax.activation.MimeType

import com.rojoma.json.v3.ast.JValue
import com.rojoma.json.v3.interpolation._

import com.socrata.http.client.Response
import com.socrata.http.common.util.Acknowledgeable

object EmptyResponse extends Response {
  val AnyMimeType: Option[MimeType] => Boolean = { mt => true }
  val EmptyJson: JValue = json"{}"

  val EmptyInputStream: InputStream with Acknowledgeable = {
    new InputStream with Acknowledgeable {
      override def acknowledge() = {}
      override def read() = -1
    }
  }

  val resultCode: Int = 0
  val charset: Charset = StandardCharsets.UTF_8
  val streamCreated: Boolean = true
  val headerNames: Set[String] = Set.empty

  def headers(name: String): Array[String] = Array.empty
  def inputStream(maxBetween: Long): InputStream with Acknowledgeable = EmptyInputStream

  override def jValue(ct: Option[MimeType] => Boolean = EmptyResponse.AnyMimeType,
                      max: Long = 0): JValue = EmptyJson

  override val contentType: Option[MimeType] =
    Some(new MimeType("application/json"))
}