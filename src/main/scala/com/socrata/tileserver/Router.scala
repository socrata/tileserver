package com.socrata.tileserver

import com.rojoma.json.v3.interpolation._
import org.slf4j.LoggerFactory

import com.socrata.http.server.implicits._
import com.socrata.http.server.responses._
import com.socrata.http.server.routing.SimpleRouteContext.{Route, Routes}
import com.socrata.http.server.routing.TypedPathComponent
import com.socrata.http.server.{HttpRequest, HttpResponse, HttpService}

// $COVERAGE-OFF$ Disabled because this is basically configuration.
class Router(versionService: HttpService,
             imageQueryTypes : String => Boolean,
             imageQueryService: (String,
                                 String,
                                 Int,
                                 Int,
                                 TypedPathComponent[Int]) => HttpService) {
  private val logger = LoggerFactory.getLogger(getClass)

  val routes = Routes(
    Route("/version", versionService),
    // domain/tiles/abcd-1234/pointColumn/z/x/y.pbf
    Route("/tiles/{String}/{String}/{Int}/{Int}/{{Int!imageQueryTypes}}",
          imageQueryService)
  )

  val notFound: HttpService = req => {
    logger.warn(s"path not found: ${req.requestPathStr}")
    NotFound ~>
      Json(json"""{error:"not found"}""")
  }

  def route(req: HttpRequest): HttpResponse =
    routes(req.requestPath).getOrElse(notFound)(req)
}
// $COVERAGE-ON$