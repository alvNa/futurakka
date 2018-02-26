package com.datio.futurakka.services

import scala.concurrent.Future

object WorkService extends WorkService {

}

trait WorkService {
  def bend(thing:String): Future[String] = {
    Future.successful {
      s"$thing bender"
    }
  }
}
