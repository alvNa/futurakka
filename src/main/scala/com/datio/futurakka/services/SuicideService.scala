package com.datio.futurakka.services

import scala.concurrent.Future

object SuicideService extends SuicideService {

}

trait SuicideService {

  def stab(): Future[String] = {
    Future.successful {
      "Your body has been stabbed"
    }
  }

  def phoneCall(): Future[String] = {
    Future.successful {
      "Slow and horrible death"
    }
  }
}
