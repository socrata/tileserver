package com.socrata.tileserver

import com.socrata.http.server.HttpResponse
import com.socrata.http.server.implicits._
import com.socrata.http.server.responses._

import util.{GeoResponse, RequestInfo, TileEncoder}

package object handlers {
  /** A protocol handler. */
  type Handler = PartialFunction[RequestInfo, ResponseBuilder]

  /** The actual function that builds a response. */
  type ResponseBuilder = (HttpResponse, GeoResponse) => HttpResponse

  /** Produce vector tiles. */
  val PbfHandler = new BaseHandler("pbf") {
    override def createResponse(reqInfo: RequestInfo,
                                base: HttpResponse,
                                encoder: TileEncoder): HttpResponse =
      base ~> ContentType("application/octet-stream") ~> ContentBytes(encoder.bytes)
  }

  /** Produce base64 encoded vector tiles. */
  val BpbfHandler = new BaseHandler("bpbf") {
    override def createResponse(reqInfo: RequestInfo,
                                base: HttpResponse,
                                encoder: TileEncoder): HttpResponse =
      base ~> Content("text/plain", encoder.base64)
  }

  /** Produce human readable debugging output. */
  val TxtHandler = new BaseHandler("txt") {
    override def createResponse(reqInfo: RequestInfo,
                                base: HttpResponse,
                                encoder: TileEncoder): HttpResponse =
      base ~> Content("text/plain", encoder.toString)
  }

  // When you try to render a png, but have no style.
  /** Rejects attempts to render pngs without $style param. */
  val UnfashionablePngHandler = new BaseHandler("png") {
    override def isDefinedAt(reqInfo: RequestInfo): Boolean =
      reqInfo.extension == extension && reqInfo.style.isEmpty

    override def createResponse(reqInfo: RequestInfo,
                                base: HttpResponse,
                                encoder: TileEncoder): HttpResponse =
      BadRequest ~>
        Content("text/plain", "Cannot render png without '$style' query parameter.")
  }
}